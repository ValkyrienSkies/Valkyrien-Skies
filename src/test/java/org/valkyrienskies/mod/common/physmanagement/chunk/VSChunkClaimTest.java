package org.valkyrienskies.mod.common.physmanagement.chunk;

public class VSChunkClaimTest {

    /*
    @Test
    public void testChunkLongs() {
        VSChunkClaim claim = new VSChunkClaim(10, 10, 10);

        Set<Long> expected = new HashSet<>();
        ImmutableSet<Long> actual = claim.getChunkLongs();

        for (int x = claim.minX(); x <= claim.maxX(); x++) {
            for (int z = claim.minZ(); z <= claim.maxZ(); z++) {
                expected.add(ChunkPos.asLong(x, z));
            }
        }

        assertThat(actual, containsInAnyOrder(expected.toArray()));
    }

    @Test
    public void testMinMaxGetters() {
        VSChunkClaim claim = new VSChunkClaim(10, 15, 10);
        assertEquals(claim.minX(), 0);
        assertEquals(claim.maxX(), 20);
        assertEquals(claim.minZ(), 5);
        assertEquals(claim.maxZ(), 25);
    }

    @Test
    public void testIterator() {
        VSChunkClaim claim = new VSChunkClaim(10, 10, 10);
        Iterator<ChunkPos> iterator = claim.iterator();

        List<ChunkPos> expectedValues = new ArrayList<>();
        List<ChunkPos> actualValues = new ArrayList<>();

        for (int x = claim.minX(); x <= claim.maxX(); x++) {
            for (int z = claim.minZ(); z <= claim.maxZ(); z++) {
                ChunkPos expected = new ChunkPos(x, z);
                ChunkPos actual = iterator.next();

                expectedValues.add(expected);
                actualValues.add(actual);
            }
        }

        assertThat(actualValues, containsInAnyOrder(expectedValues.toArray()));
        assertEquals(expectedValues, actualValues);
    }
    
     */

}
