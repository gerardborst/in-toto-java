package io.github.in_toto.models;

import io.github.in_toto.models.Artifact.ArtifactHash;
import io.github.in_toto.models.Artifact.ArtifactHash.HashAlgorithm;
import io.github.in_toto.models.Link;
import io.github.in_toto.models.Link.LinkBuilder;
import io.github.in_toto.keys.RSAKey;
import io.github.in_toto.exceptions.ValueError;
import io.github.in_toto.keys.Key;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.List;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.io.TempDir;

import com.google.gson.reflect.TypeToken;

import org.junit.rules.ExpectedException;
import org.junit.Rule;

/**
 * Link-specific tests
 */
 
@DisplayName("Link-specific tests")
@TestInstance(Lifecycle.PER_CLASS)
class LinkTest
{
    private LinkBuilder linkBuilder =  new LinkBuilder("test");
	private Link link = linkBuilder.build();
    private Key key = RSAKey.read("src/test/resources/link_test/somekey.pem");
    private FileTransporter transporter = new FileTransporter();
    private Type metablockType = new TypeToken<Metablock<Link>>() {}.getType();
    
    @TempDir
    Path temporaryFolder;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    @DisplayName("Test Link Constructor")
    public void testLinkContructorEqual()
    {
        // test a link object
    
        assertEquals("test",link.getName());
        assertNotEquals(null,link.getName());
    }

    @Test
    @DisplayName("Test Valid Link Object Types")
    public void testValidObject()
    {
        // test link data types

        assertTrue(link instanceof Link);
        assertTrue(link.getByproducts() instanceof Map);
        assertTrue(link.getEnvironment() instanceof Map);
        assertTrue(link.getName() instanceof String);
        assertTrue(link.getProducts() instanceof Set);
        assertTrue(link.getMaterials() instanceof Set);
        assertTrue(link.getCommand() instanceof List);
    }

    @Test
    @DisplayName("Validate Link addMaterials")
    public void testValidMaterial() throws IOException, ValueError
    {

        String keyFile = Files.createFile(temporaryFolder.resolve("alice")).toString();
        
        Artifact pathArtifact = Artifact.recordArtifacts(Arrays.asList(keyFile), null, null).iterator().next();;
        
        linkBuilder.addMaterial(Arrays.asList(keyFile));
        
        Link link = linkBuilder.build();

        assertTrue(link.getMaterials().contains(pathArtifact));
    }

    @Test
    @DisplayName("Validate Link addProducts")
    public void testValidProduct() throws IOException, ValueError
    {
        String path = Files.createFile(temporaryFolder.resolve("bob")).toString();
        
        Artifact pathArtifact = Artifact.recordArtifacts(Arrays.asList(path), null, null).iterator().next();; 
        
        linkBuilder.addProduct(Arrays.asList(path));
        
        Link link = linkBuilder.build();
        
        Set<Artifact> product = link.getProducts();
        Artifact entry = product.iterator().next();
        assertEquals(entry, pathArtifact);
    }

    @Test
    @DisplayName("Validate Link setByproducts")
    public void testValidByproduct()
    {
        HashMap<String, Object> byproduct = new HashMap<>();
        byproduct.put("stdin","");
        
        linkBuilder.setByproducts(byproduct);
        
        Link link = linkBuilder.build();
        assertEquals(byproduct, link.getByproducts());
    }

    @Test
    @DisplayName("Validate Link setEnvironments")
    public void testValidEnvironment()
    {
        HashMap<String, Object> environment = new HashMap<>();
        environment.put("variables", "<ENV>");
        
        linkBuilder.setEnvironment(environment);
        Link link = linkBuilder.build();
        assertEquals(environment, link.getEnvironment());
    }

    @Test
    @DisplayName("Validate Link setCommand")
    public void testValidCommand()
    {
        ArrayList<String> command = new ArrayList<String>();
        command.add("<COMMAND>");
        linkBuilder.setCommand(command);
        
        Link link = linkBuilder.build();
        assertEquals(command.get(0), link.getCommand().get(0));
    }

    @AfterAll
    @Test
    @DisplayName("Validate Link sign & dump")
    public void testLinkSignDump() throws URISyntaxException
    {
        Metablock<Link> metablock = new Metablock<Link>(link, null);
        metablock.sign(key);
    	URI uri = new URI("test.0b70eafb.link");
        transporter.dump(uri, metablock);
        File fl = new File("test.0b70eafb.link");
        assertTrue(fl.exists());
        fl.delete();
    }

    @Test
    @DisplayName("Validate link serialization and deserialization with artifacts from object")
    public void testLinkSerializationWithArtifactsFromObject() throws IOException, URISyntaxException, ValueError
    {
    	LinkBuilder testLinkBuilder = new LinkBuilder("serialize");
    	testLinkBuilder.setBasePath("src/test/resources/link_test/serialize");
        
        Set<Artifact> artifacts = Artifact.recordArtifacts(Arrays.asList("foo", "baz", "bar"), null, "src/test/resources/link_test/serialize");
        
        Artifact pathArtifact1 = artifacts.iterator().next();
        Artifact pathArtifact2 = artifacts.iterator().next();
        Artifact pathArtifact3 = artifacts.iterator().next();        

        testLinkBuilder.addProduct(Arrays.asList(""));
        testLinkBuilder.addMaterial(Arrays.asList(""));
        
        Metablock<Link> testMetablockLink = new Metablock<Link>(testLinkBuilder.build(), null);
        testMetablockLink.sign(key);
        
        URI linkFile = new URI(testMetablockLink.getSignatures().get(0).getKeyId());
        
        transporter.dump(linkFile, testMetablockLink);

        Metablock<Link> newLinkMetablock = transporter.load(linkFile, metablockType);
        File tempFile = new File(linkFile.getPath());
        tempFile.delete();
        
        newLinkMetablock.sign(key);
        
        assertEquals(newLinkMetablock.signatures.get(0).getSig(), testMetablockLink.signatures.get(0).getSig());

        assertEquals(newLinkMetablock.signed.getName(), testMetablockLink.signed.getName());
        assertTrue(newLinkMetablock.signed.getProducts().contains(pathArtifact1));
        assertTrue(newLinkMetablock.signed.getProducts().contains(pathArtifact2));
        assertTrue(newLinkMetablock.signed.getProducts().contains(pathArtifact3));
        assertTrue(newLinkMetablock.signed.getMaterials().contains(pathArtifact1));
        assertTrue(newLinkMetablock.signed.getMaterials().contains(pathArtifact2));
        assertTrue(newLinkMetablock.signed.getMaterials().contains(pathArtifact3));
        
        Metablock<Link> metablockFromFile = transporter.load(new URI("src/test/resources/link_test/serialize/serialize.link"), metablockType);
        metablockFromFile.sign(key);
        
        assertEquals(metablockFromFile.signed.getName(), testMetablockLink.signed.getName());
        assertTrue(metablockFromFile.signed.getProducts().contains(pathArtifact1));
        assertTrue(metablockFromFile.signed.getProducts().contains(pathArtifact2));
        assertTrue(metablockFromFile.signed.getProducts().contains(pathArtifact3));
        assertTrue(metablockFromFile.signed.getMaterials().contains(pathArtifact1));
        assertTrue(metablockFromFile.signed.getMaterials().contains(pathArtifact2));
        assertTrue(metablockFromFile.signed.getMaterials().contains(pathArtifact3));        
        //assertEquals(metablockFromFile.signatures.get(0).getSig(), testMetablockLink.signatures.get(0).getSig());
    }

    
    @Test
    @DisplayName("Validate link serialization and de-serialization with artifacts from file")
    public void testLinkDeSerializationWithArtifactsFromFile() throws URISyntaxException
    {
    	Artifact testproduct = new Artifact("demo-project/foo.py", new ArtifactHash(HashAlgorithm.sha256, "ebebf8778035e0e842a4f1aeb92a601be8ea8e621195f3b972316c60c9e12235"));

        Metablock<Link> testMetablockLink = transporter.load(new URI("src/test/resources/link_test/clone.776a00e2.link"), metablockType);
        assertTrue(testMetablockLink.signed.getName() != null);
        assertEquals(testMetablockLink.signed.getName(), "clone");
        assertTrue(testMetablockLink.signed.getProducts().contains(testproduct));

        URI linkFile = new URI(testMetablockLink.getSignatures().get(0).getKeyId());
        
        transporter.dump(linkFile, testMetablockLink);

        Metablock<Link> newLinkMetablock = transporter.load(linkFile, metablockType);
        File tempFile = new File(linkFile.getPath());
        tempFile.delete();

        assertEquals(newLinkMetablock.signed.getName(), testMetablockLink.signed.getName());
        assertTrue(newLinkMetablock.signed.getProducts().contains(testproduct));
    }

    @Test
    @DisplayName("Test Apply Exclude Patterns")
    public void testApplyExcludePatterns() throws IOException, ValueError
    {
    	LinkBuilder testLinkBuilder = new LinkBuilder("sometestname");

        String path1 = Files.createFile(temporaryFolder.resolve("foo")).toString();
        String path2 = Files.createFile(temporaryFolder.resolve("bar")).toString();
        String path3 = Files.createFile(temporaryFolder.resolve("baz")).toString();
        
        Artifact pathArtifact3 = Artifact.recordArtifacts(Arrays.asList(path3), null, null).iterator().next();;

        String pattern = "**{foo,bar}";

        testLinkBuilder.setExcludePatterns(pattern);

        testLinkBuilder.addProduct(Arrays.asList(path1, path2, path3));
        testLinkBuilder.addMaterial(Arrays.asList(path1, path2, path3));
        
        Link testLink = testLinkBuilder.build();

        Set<Artifact> product = testLink.getProducts();
        assertEquals(product.size(), 1);

        assertTrue(product.contains(pathArtifact3));

        Set<Artifact> material = testLink.getMaterials();
        assertEquals(material.size(), 1);

        assertTrue(material.contains(pathArtifact3));
    }

    @Test
    @DisplayName("Test Apply Exclude Default Patterns")
    public void testApplyExcludeDefaultPatterns() throws IOException, ValueError
    {
    	LinkBuilder testLinkBuilder = new LinkBuilder("sometestname");

        String path1 = Files.createFile(temporaryFolder.resolve("foo.link")).toString();
        String path2 = Files.createFile(temporaryFolder.resolve("bar")).toString();
        Path path3 = Files.createDirectory(temporaryFolder.resolve(".git"));
        
        String path4 = Files.createFile(Paths.get(path3.toString(),"baz")).toString();        
        Artifact pathArtifact2 = Artifact.recordArtifacts(Arrays.asList(path2), null, null).iterator().next();

        testLinkBuilder.addProduct(Arrays.asList(path1, path2, path4));
        testLinkBuilder.addMaterial(Arrays.asList(path1, path2, path4));
        
        Link testLink = testLinkBuilder.build();

        Set<Artifact> product = testLink.getProducts();
        assertEquals(product.size(), 1);

        assertTrue(product.contains(pathArtifact2));

        Set<Artifact> material = testLink.getMaterials();
        assertEquals(material.size(), 1);

        assertTrue(material.contains(pathArtifact2));
    }

    @Test
    @DisplayName("Test Apply Exclude All")
    public void testApplyExcludeAll() throws IOException, ValueError
    {
    	LinkBuilder testLinkBuilder = new LinkBuilder("sometestname");

        String path1 = Files.createFile(temporaryFolder.resolve("foo")).toString();
        String path2 = Files.createFile(temporaryFolder.resolve("bar")).toString();
        String path3 = Files.createFile(temporaryFolder.resolve("baz")).toString();

        String pattern = "**";

        testLinkBuilder.setExcludePatterns(pattern);

        testLinkBuilder.addProduct(Arrays.asList(path1, path2, path3));
        testLinkBuilder.addMaterial(Arrays.asList(path1, path2, path3));
        
        Link testLink = testLinkBuilder.build();

        Set<Artifact> product = testLink.getProducts();
        assertEquals(product.size(), 0);
        assertFalse(product.iterator().hasNext());

        Set<Artifact> material = testLink.getMaterials();
        assertEquals(material.size(), 0);
        assertFalse(material.iterator().hasNext());
    }

    @Test
    @DisplayName("Test Apply Exclude Multiple Star")
    public void testApplyExcludeMultipleStar() throws IOException, ValueError
    {
    	LinkBuilder testLinkBuilder = new LinkBuilder("sometestname");

        String path1 = Files.createFile(temporaryFolder.resolve("foo")).toString();
        String path2 = Files.createFile(temporaryFolder.resolve("bar")).toString();
        String path3 = Files.createFile(temporaryFolder.resolve("baz")).toString();
        
        Artifact pathArtifact1 = Artifact.recordArtifacts(Arrays.asList(path1), null, null).iterator().next();

        String pattern = "**a**";

        testLinkBuilder.setExcludePatterns(pattern);

        testLinkBuilder.addProduct(Arrays.asList(path1, path2, path3));
        testLinkBuilder.addMaterial(Arrays.asList(path1, path2, path3));
        
        Link testLink = testLinkBuilder.build();

        Set<Artifact> product = testLink.getProducts();
        assertEquals(product.size(), 1);

        assertTrue(product.contains(pathArtifact1));

        Set<Artifact> material = testLink.getMaterials();
        assertEquals(material.size(), 1);

        assertTrue(material.contains(pathArtifact1));
    }

    @Test
    @DisplayName("Test Apply Exclude Question Mark")
    public void testApplyExcludeQuestionMark() throws IOException, ValueError
    {
    	LinkBuilder testLinkBuilder = new LinkBuilder("sometestname");

        String path1 = Files.createFile(temporaryFolder.resolve("foo")).toString();
        String path2 = Files.createFile(temporaryFolder.resolve("barfoo")).toString();
        String path3 = Files.createFile(temporaryFolder.resolve("bazfoo")).toString();
        
        Artifact pathArtifact1 = Artifact.recordArtifacts(Arrays.asList(path1), null, null).iterator().next();

        String pattern = "**ba?foo";

        testLinkBuilder.setExcludePatterns(pattern);

        testLinkBuilder.addProduct(Arrays.asList(path1, path2, path3));
        testLinkBuilder.addMaterial(Arrays.asList(path1, path2, path3));
        
        Link testLink = testLinkBuilder.build();

        Set<Artifact> product = testLink.getProducts();
        assertEquals(product.size(), 1);
        
        assertTrue(product.contains(pathArtifact1));

        Set<Artifact> material = testLink.getProducts();
        assertEquals(material.size(), 1);

        assertTrue(material.contains(pathArtifact1));
    }

    @Test
    @DisplayName("Test Apply Exclude Sequence")
    public void testApplyExcludeSeq() throws IOException, ValueError
    {
    	LinkBuilder testLinkBuilder = new LinkBuilder("sometestname");

        String path1 = Files.createFile(temporaryFolder.resolve("baxfoo")).toString();
        String path2 = Files.createFile(temporaryFolder.resolve("bazfoo")).toString();
        String path3 = Files.createFile(temporaryFolder.resolve("barfoo")).toString();
        
        Artifact pathArtifact3 = Artifact.recordArtifacts(Arrays.asList(path3), null, null).iterator().next();

        String pattern = "**ba[xz]foo";

        testLinkBuilder.setExcludePatterns(pattern);
        
        testLinkBuilder.addProduct(Arrays.asList(path1, path2, path3));
        testLinkBuilder.addMaterial(Arrays.asList(path1, path2, path3));
        
        Link testLink = testLinkBuilder.build();

        Set<Artifact> product = testLink.getProducts();
        assertEquals(product.size(), 1);

        assertTrue(product.contains(pathArtifact3));

        Set<Artifact> material = testLink.getMaterials();
        assertEquals(material.size(), 1);

        assertTrue(material.contains(pathArtifact3));
    }


    @Test
    @DisplayName("Test Apply Exclude Negate Sequence")
    public void testApplyExcludeNegSeq() throws IOException, ValueError
    {
    	LinkBuilder testLinkBuilder = new LinkBuilder("sometestname");

        String path1 = Files.createFile(temporaryFolder.resolve("baxfoo")).toString();
        String path2 = Files.createFile(temporaryFolder.resolve("bazfoo")).toString();
        String path3 = Files.createFile(temporaryFolder.resolve("barfoo")).toString();
        
        Artifact pathArtifact3 = Artifact.recordArtifacts(Arrays.asList(path3), null, null).iterator().next();

        String pattern = "**ba[!r]foo";

        testLinkBuilder.setExcludePatterns(pattern);

        testLinkBuilder.addProduct(Arrays.asList(path1, path2, path3));
        testLinkBuilder.addMaterial(Arrays.asList(path1, path2, path3));
        
        Link testLink = testLinkBuilder.build();

        Set<Artifact> product = testLink.getProducts();
        assertEquals(product.size(), 1);

        assertTrue(product.contains(pathArtifact3));

        Set<Artifact> material = testLink.getMaterials();
        assertEquals(material.size(), 1);

        assertTrue(material.contains(pathArtifact3));
    }

}
