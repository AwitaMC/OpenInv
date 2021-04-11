/*
 * Copyright (C) 2011-2021 lishid. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.lishid.openinv.search;

import com.github.jikoo.planarwrappers.tuple.Pair;
import com.lishid.openinv.OpenInv;
import io.papermc.lib.PaperLib;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

public class ChunkBucket implements SearchBucket {

    private final OpenInv plugin;
    private final World world;
    private final boolean load;
    private final List<Pair<Integer, Integer>> chunks;
    protected int index = -1;

    public ChunkBucket(@NotNull OpenInv plugin, @NotNull World world, int chunkX, int chunkZ, int radius, boolean load) {
        this.plugin = plugin;
        this.world = world;
        this.load = load;
        this.chunks = new ArrayList<>();
        // Order chunks based (loosely) on proximity.
        spiralRectangle(radius, (deltaX, deltaZ) -> chunks.add(new Pair<>(chunkX + deltaX, chunkZ + deltaZ)));
    }

    @Override
    public @NotNull Matchable next() throws IndexOutOfBoundsException {
        Pair<Integer, Integer> chunkCoords = chunks.get(++index);

        if (!world.isChunkGenerated(chunkCoords.getLeft(), chunkCoords.getRight())) {
            return Matchable.EMPTY;
        }

        if (!load && !world.isChunkLoaded(chunkCoords.getLeft(), chunkCoords.getRight())) {
            return Matchable.EMPTY;
        }

        // If async chunk loading is not available, load chunk when checking.
        if (!PaperLib.isPaper()) {
            return new MatchableChunk(plugin, world, chunkCoords.getLeft(), chunkCoords.getRight());
        }

        CompletableFuture<Chunk> chunkAt = PaperLib.getChunkAtAsync(world, chunkCoords.getLeft(), chunkCoords.getRight());

        Chunk chunk;
        try {
            chunk = chunkAt.get();
        } catch (InterruptedException | ExecutionException e) {
            return matcher -> MatchResult.NO_MATCH;
        }

        // Ensure chunk won't unload until we search it during next server tick.
        chunk.addPluginChunkTicket(plugin);

        return new MatchableChunk(plugin, chunk);
    }

    @Override
    public boolean hasNext() {
        return index < chunks.size() - 1;
    }

    @Override
    public int size() {
        return chunks.size();
    }

    @Override
    public void cleanUp() {
        // Just in case, remove all chunk tickets after search completion.
        for (Chunk loadedChunk : world.getLoadedChunks()) {
            loadedChunk.removePluginChunkTicket(plugin);
        }
    }
    /**
     * Produce a series of integer coordinates starting at 0, 0 that extend outward to form a centered square,
     * producing closer proximity results sooner than a traditional double for loop from min to max.
     *
     * @param radius the radius of the square
     * @param coordConsumer the method consuming the values produced
     */
    private static void spiralRectangle(int radius, BiConsumer<Integer, Integer> coordConsumer) {
        for (int x = 0; x <= radius; ++x) {
            for (int z = 0; z <= radius; ++z) {
                coordConsumer.accept(x, z);
                if (x != 0) {
                    coordConsumer.accept(-x, z);
                    if (z != 0) {
                        coordConsumer.accept(-x, -z);
                    }
                }
                if (z != 0) {
                    coordConsumer.accept(x, -z);
                }
            }
        }
    }

}
