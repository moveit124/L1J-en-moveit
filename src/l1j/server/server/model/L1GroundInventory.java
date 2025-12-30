package l1j.server.server.model;

import java.util.*;
import java.util.concurrent.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l1j.server.server.datatables.NpcTable;
import l1j.server.server.datatables.SpawnTable;
import l1j.server.server.encryptions.IdFactory;
import l1j.server.server.model.Instance.L1ItemInstance;
import l1j.server.server.model.Instance.L1MonsterInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.map.L1Map;
import l1j.server.server.model.map.L1WorldMap;
import l1j.server.server.serverpackets.S_DropItem;
import l1j.server.server.serverpackets.S_RemoveObject;
import l1j.server.server.templates.L1Npc;
import l1j.server.server.types.Point;

public class L1GroundInventory extends L1Inventory {
    private static final long serialVersionUID = 1L;
    private static final int SLIME_SPAWN_TIME = 15 * 60 * 1000; // 15 minutes
    private static final int SLIME_RADIUS = 15;
    private static final int GRID_SIZE = 20;
    private static final ScheduledExecutorService _scheduler = Executors.newScheduledThreadPool(1);
    private static final Random random = new Random();

    private static final Map<Short, Map<String, List<L1ItemInstance>>> gridSpawnCandidates = new ConcurrentHashMap<>();
    private static final Map<Integer, Long> itemDropTimes = new ConcurrentHashMap<>();
    private static final Map<String, Integer> droppedItemCounts = new ConcurrentHashMap<>();
    private static final Set<String> activeSlimeKeys = ConcurrentHashMap.newKeySet();
    private static final int MAX_ITEMS_PER_LOCATION = 50;

    public L1GroundInventory(int objectId, int x, int y, short map) {
        setId(objectId);
        setX(x);
        setY(y);
        setMap(map);
        L1World.getInstance().addVisibleObject(this);
    }

    @Override
    public void onPerceive(L1PcInstance perceivedFrom) {
        for (L1ItemInstance item : getItems()) {
            if (!perceivedFrom.knownsObject(item)) {
                perceivedFrom.addKnownObject(item);
                perceivedFrom.sendPackets(new S_DropItem(item));
            }
        }
    }

    public void insertItem(L1ItemInstance item) {
        for (L1PcInstance pc : L1World.getInstance().getRecognizePlayer(item)) {
            pc.sendPackets(new S_DropItem(item));
            pc.addKnownObject(item);
        }

        if (isSpawnedItem(item)) {
            String locationKey = item.getX() + "," + item.getY() + "," + item.getMapId();
            int currentItemCount = droppedItemCounts.getOrDefault(locationKey, 0);

            if (currentItemCount >= MAX_ITEMS_PER_LOCATION) {
                return;
            }

            droppedItemCounts.put(locationKey, currentItemCount + 1);
            return;
        }

        String gridKey = getGridKey(item.getX(), item.getY());
        gridSpawnCandidates.computeIfAbsent(item.getMapId(), k -> new ConcurrentHashMap<>())
                .computeIfAbsent(gridKey, k -> new CopyOnWriteArrayList<>())
                .add(item);

        itemDropTimes.put(item.getId(), System.currentTimeMillis());
    }

    @Override
    public void updateItem(L1ItemInstance item) {
        for (L1PcInstance pc : L1World.getInstance().getRecognizePlayer(item)) {
            pc.sendPackets(new S_DropItem(item));
        }
    }

    private static String getGridKey(int x, int y) {
        return (x / GRID_SIZE) + "," + (y / GRID_SIZE);
    }

    private static void spawnSlime(int x, int y, int mapId, String gridKey) {
        String slimeKey = mapId + ":" + gridKey;
        if (activeSlimeKeys.contains(slimeKey)) return;

        int[] mobOptions = {45023, 70984, 45025, 45060}; // Boar, Cow, Gremlin, Slime
        int slimeId = mobOptions[random.nextInt(mobOptions.length)];

        L1Npc slimeNpc = NpcTable.getInstance().getTemplate(slimeId);
        if (slimeNpc == null) return;

        L1MonsterInstance slime = new L1MonsterInstance(slimeNpc) {
            @Override
            public void deleteMe() {
                super.deleteMe();
                activeSlimeKeys.remove(slimeKey);
            }
        };

        L1Map map = L1WorldMap.getInstance().getMap((short) mapId);
        Point candidate = null;

        // Try up to 50 times to find a passable nearby location
        for (int i = 0; i < 50; i++) {
            int nx = x + random.nextInt(11) - 5; // -5 to +5
            int ny = y + random.nextInt(11) - 5;
            Point point = new Point(nx, ny);

            if (map.isInMap(point) && map.isPassable(point)) {
                candidate = point;
                break;
            }
        }

        // Fallback if no valid location found
        if (candidate == null) {
            candidate = new Point(x, y);
        }

        slime.setId(IdFactory.getInstance().nextId());
        slime.setX(candidate.getX());
        slime.setY(candidate.getY());
        slime.setMap((short) mapId);
        slime.setHeading(random.nextInt(8));

        L1World.getInstance().storeObject(slime);
        L1World.getInstance().addVisibleObject(slime);
        activeSlimeKeys.add(slimeKey);
        
        // 🔻 Schedule item deletion after 5 minutes
        _scheduler.schedule(() -> {
            Map<String, List<L1ItemInstance>> gridMap = gridSpawnCandidates.get((short) mapId);
            if (gridMap != null) {
                List<L1ItemInstance> gridItems = gridMap.get(gridKey);
                if (gridItems != null) {
                    for (L1ItemInstance item : new ArrayList<>(gridItems)) {
                        // Must match the inventory at the ground location
                        Collection<L1Object> objs = L1World.getInstance().getVisibleObjects(item, 0);
                        for (L1Object obj : objs) {
                            if (obj instanceof L1GroundInventory) {
                                ((L1GroundInventory) obj).deleteItem(item);
                                _log.info("Slime system: deleted item " + item.getName() + " after 5 minutes.");
                                break;
                            }
                        }
                    }
                }
            }
        }, 5, TimeUnit.MINUTES);

        // 🔻 Schedule slime death after 10 minutes
        _scheduler.schedule(() -> {
            slime.receiveDamage(null, 999999);
            _log.info("Slime system: killed slime after 10 minutes.");
        }, 10, TimeUnit.MINUTES);
    }
    

    @Override
    public void deleteItem(L1ItemInstance item) {
        for (L1PcInstance pc : L1World.getInstance().getRecognizePlayer(item)) {
            pc.sendPackets(new S_RemoveObject(item));
            pc.removeKnownObject(item);
        }

        _items.remove(item);
        if (_items.isEmpty()) {
            L1World.getInstance().removeVisibleObject(this);
        }

        short mapId = item.getMapId();
        String gridKey = getGridKey(item.getX(), item.getY());
        Map<String, List<L1ItemInstance>> mapGrids = gridSpawnCandidates.get(mapId);
        if (mapGrids != null) {
            List<L1ItemInstance> list = mapGrids.get(gridKey);
            if (list != null) list.remove(item);
        }
        itemDropTimes.remove(item.getId());
    }

    private boolean isSpawnedItem(L1ItemInstance item) {
        if (item.getItem().getItemId() == 40515) {
            if (item.getX() >= ElementalStoneGenerator.getFirstX() && item.getX() <= ElementalStoneGenerator.getLastX() &&
                item.getY() >= ElementalStoneGenerator.getFirstY() && item.getY() <= ElementalStoneGenerator.getLastY()) {
                for (L1Object obj : L1World.getInstance().getVisibleObjects(item, 3)) {
                    if (obj instanceof L1ItemInstance) {
                        L1ItemInstance nearbyItem = (L1ItemInstance) obj;
                        if (nearbyItem.getItem().getItemId() == 40515) {
                            return true;
                        }
                    }
                }
                return true;
            }
        }

        for (L1Spawn spawn : SpawnTable.getInstance().get_spawntable().values()) {
            if (spawn.getLocX() == item.getX() && spawn.getLocY() == item.getY() && spawn.getMapId() == item.getMapId()) {
                if (spawn.getNpcId() == item.getItem().getItemId()) {
                    return true;
                }
            }
        }

        return false;
    }

    static {
        _scheduler.scheduleAtFixedRate(() -> {
            long currentTime = System.currentTimeMillis();

            for (Map.Entry<Short, Map<String, List<L1ItemInstance>>> mapEntry : gridSpawnCandidates.entrySet()) {
                short mapId = mapEntry.getKey();
                Map<String, List<L1ItemInstance>> gridMap = mapEntry.getValue();

                for (Map.Entry<String, List<L1ItemInstance>> gridEntry : gridMap.entrySet()) {
                    String gridKey = gridEntry.getKey();
                    List<L1ItemInstance> items = gridEntry.getValue();

                    if (items.isEmpty()) continue;

                    boolean shouldSpawn = false;
                    for (L1ItemInstance item : items) {
                        long dropTime = itemDropTimes.getOrDefault(item.getId(), 0L);
                        if (currentTime - dropTime >= SLIME_SPAWN_TIME) {
                            shouldSpawn = true;
                            break;
                        }
                    }

                    if (shouldSpawn) {
                        L1ItemInstance spawnSource = items.get(0);
                        spawnSlime(spawnSource.getX(), spawnSource.getY(), mapId, gridKey);

                        for (L1ItemInstance item : items) {
                            itemDropTimes.put(item.getId(), System.currentTimeMillis()); // Reset timer
                        }
                    }
                }
            }
        }, 30, 30, TimeUnit.SECONDS);
    }

    private static Logger _log = LoggerFactory.getLogger(L1GroundInventory.class.getName());
}
