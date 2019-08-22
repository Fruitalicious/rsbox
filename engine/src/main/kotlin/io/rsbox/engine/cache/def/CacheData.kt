package io.rsbox.engine.cache.def

import io.netty.buffer.Unpooled
import io.rsbox.engine.Engine
import io.rsbox.engine.cache.def.impl.*
import io.rsbox.engine.model.world.ChunkSet
import io.rsbox.engine.model.world.Tile
import io.rsbox.engine.model.world.World
import io.rsbox.engine.service.ServiceManager
import io.rsbox.engine.service.impl.XteaKeyService
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import mu.KLogging
import net.runelite.cache.ConfigType
import net.runelite.cache.IndexType
import net.runelite.cache.definitions.loaders.MapLoader
import net.runelite.cache.region.Region
import java.io.FileNotFoundException
import java.lang.IllegalArgumentException

/**
 * @author Kyle Escobar
 */

class CacheData : io.rsbox.api.cache.CacheData {
    private val defs = Object2ObjectOpenHashMap<Class<out CacheDef>, Map<Int, *>>()

    private var xteaKeyService: XteaKeyService = ServiceManager[XteaKeyService::class.java]!!

    private val store = Engine.cacheStore

    fun loadAll() {
        load(CacheAnimation::class.java)
        logger.info("Loaded ${getCount(CacheAnimation::class.java)} animation defs.")

        load(CacheEnum::class.java)
        logger.info("Loaded ${getCount(CacheEnum::class.java)} enum defs.")

        load(CacheItem::class.java)
        logger.info("Loaded ${getCount(CacheItem::class.java)} item defs.")

        load(CacheNpc::class.java)
        logger.info("Loaded ${getCount(CacheNpc::class.java)} npc defs.")

        load(CacheObject::class.java)
        logger.info("Loaded ${getCount(CacheObject::class.java)} object defs.")

        load(CacheVarbit::class.java)
        logger.info("Loaded ${getCount(CacheVarbit::class.java)} varbit defs.")

        load(CacheVarp::class.java)
        logger.info("Loaded ${getCount(CacheVarp::class.java)} varp defs.")
    }

    fun loadRegions(world: World, chunks: ChunkSet, regions: IntArray) {
        var loaded = 0
        regions.forEach { region ->
            if(chunks.activeRegions.add(region)) {
                if(createRegion(world, region)) {
                    loaded++
                }
            }
        }

        logger.info("Loaded $loaded world regions.")
    }

    fun <T : CacheDef> load(type: Class<out T>) {
        val configType = this.classToType(type) ?: throw Exception("Unknown cache type class ${type.simpleName}.")

        val configs = store.getIndex(IndexType.CONFIGS) ?: throw FileNotFoundException("Cache data was not found. Make sure you run server:setup gradle task first.")
        val archive = configs.getArchive(configType.id)!!
        val files = archive.getFiles(store.storage.loadArchive(archive)!!).files

        val defs = Int2ObjectOpenHashMap<T?>(files.size + 1)

        for(i in 0 until files.size) {
            val def = createDef(type, files[i].fileId, files[i].contents)
            defs[files[i].fileId] = def
        }
        this.defs[type] = defs
    }

    @Suppress("UNCHECKED_CAST")
    fun < T: CacheDef> createDef(type: Class<out T>, id: Int, data: ByteArray) : T {
        val def: CacheDef = when(type) {
            CacheAnimation::class.java -> CacheAnimation(id)
            CacheEnum::class.java -> CacheEnum(id)
            CacheItem::class.java -> CacheItem(id)
            CacheNpc::class.java -> CacheNpc(id)
            CacheObject::class.java -> CacheObject(id)
            CacheVarbit::class.java -> CacheVarbit(id)
            CacheVarp::class.java -> CacheVarp(id)
            else -> throw IllegalArgumentException("Unhandled class type ${type.simpleName}.")
        }

        val buf = Unpooled.wrappedBuffer(data)
        def.decode(buf)
        buf.release()
        return def as T
    }

    fun getCount(type: Class<*>) = defs[type]!!.size

    @Suppress("UNCHECKED_CAST")
    fun <T : CacheDef> get(type: Class<out T>, id: Int): T {
        return (defs[type]!!)[id] as T
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : io.rsbox.api.cache.CacheDef> get(type: Class<out T>, id: Int): T {
        return get(type, id)
    }

    private fun classToType(clazz: Class<out CacheDef>): ConfigType? {
        return when(clazz) {
            CacheAnimation::class.java -> ConfigType.SEQUENCE
            CacheEnum::class.java -> ConfigType.ENUM
            CacheItem::class.java -> ConfigType.ITEM
            CacheNpc::class.java -> ConfigType.NPC
            CacheObject::class.java -> ConfigType.OBJECT
            CacheVarbit::class.java -> ConfigType.VARBIT
            CacheVarp::class.java -> ConfigType.VARPLAYER
            else -> null
        }
    }

    fun createRegion(world: World, regionId: Int): Boolean {
        val regionIndex = store.getIndex(IndexType.MAPS)

        val x = regionId shr 8
        val z = regionId and 0xFF

        val mapArchive = regionIndex.findArchiveByName("m${x}_$z") ?: return false
        val landArchive = regionIndex.findArchiveByName("l${x}_$z") ?: return false
        val mapData = mapArchive.decompress(store.storage.loadArchive(mapArchive))

        if(mapData == null) {
            logger.error("Unable to load map data for region $regionId.")
            return false
        }

        val mapDef = MapLoader().load(x, z, mapData)

        val cacheRegion = Region(regionId)
        cacheRegion.loadTerrain(mapDef)

        /**
         * Collision stuff
         */
        val blocked = hashSetOf<Tile>()
        val bridges = hashSetOf<Tile>()


        return true
    }

    companion object : KLogging()
}