package org.terasology.portals;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.componentSystem.UpdateSubscriberSystem;
import org.terasology.components.LocalPlayerComponent;
import org.terasology.components.world.LocationComponent;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.RegisterComponentSystem;
import org.terasology.events.ActivateEvent;
import org.terasology.game.CoreRegistry;
import org.terasology.miniion.components.SimpleMinionAIComponent;
import org.terasology.miniion.minionenum.MinionBehaviour;
import org.terasology.miniion.minionenum.ZoneType;
import org.terasology.miniion.components.MinionComponent;
import org.terasology.model.structures.BlockPosition;
import org.terasology.rendering.logic.MeshComponent;
import org.terasology.world.block.BlockComponent;
import org.terasology.math.Vector3i;
import org.terasology.miniion.utilities.*;


import javax.vecmath.Color4f;
import javax.vecmath.Vector3f;
import java.util.ArrayList;
import java.util.Set;

/**
 * System to start building Civilization
 *
 * @author Stuart
 */
@RegisterComponentSystem
public class CivilizationSystem implements UpdateSubscriberSystem {

    protected EntityManager entityManager;

    Zone myZone = new Zone();
    boolean fake = true;
    private long tick = 0;
    private long classLastTick = 0;


    private static final Logger logger = LoggerFactory.getLogger(CivilizationSystem.class);
    public Zone assignedzone;
    public MinionBehaviour minionBehavior = MinionBehaviour.Gather;

    @Override
    public void initialise() {
        entityManager = CoreRegistry.get(EntityManager.class);
    }

    @Override
    public void shutdown() {
    }

    /**
     * Responsible for tick update - see if we should review thresholds (like making a new queen)
     *
     * @param delta time step since last update
     */
    public void update(float delta) {
        // Do a time check to see if we should even bother calculating stuff (really only needed every second or so)
        // Keep a ms counter handy, delta is in seconds
        tick += delta * 1000;

        if (tick - classLastTick < 5000) {
            return;
        }
        classLastTick = tick;

        // Prep a list of the holdings we know about
        ArrayList<EntityRef> holdingEntities = new ArrayList<EntityRef>(4);

        // Go fetch all the Holdings.
        // TODO: Do we have a helper method for this? I forgot and it is late :P
        for (EntityRef holdingEntity : entityManager.iteratorEntities(HoldingComponent.class)) {
            holdingEntities.add(holdingEntity);
        }


        // For each Holding check if there are enough Spawnables to create a plain
        for (EntityRef holdingEntity : holdingEntities) {
            boolean working = false;
            HoldingComponent holdComp = holdingEntity.getComponent(HoldingComponent.class);
            if (holdComp.queenCurrent == 1 )   {
                logger.info("Make a plain!");

               // Get the location to start and end the plain, roughly centered on the Holding in question.
               if(fake = false )    {
                   logger.info("A queen!")    ;
                LocationComponent holdingLocation = holdingEntity.getComponent(LocationComponent.class);
                Vector3f originPos;
                originPos = holdingLocation.getWorldPosition();

                int holdingX = (int) originPos.x;
                int holdingY = (int) originPos.y;
                int holdingZ = (int) originPos.z;

                Vector3i convertPos = new Vector3i(holdingX, holdingY, holdingZ);
                Vector3i startPos = new Vector3i (convertPos.x - 15, convertPos.y - 15, convertPos.z);
                Vector3i endPos = new Vector3i (convertPos.x + 15, convertPos.y + 15, convertPos.z);

                myZone.setStartPosition(startPos);
                myZone.setEndPosition(endPos);
                   working = true;
               }
               else if (holdingEntity.hasComponent(BlockComponent.class)){
                   logger.info("A block holding!");
                   Vector3i originPos = holdingEntity.getComponent(BlockComponent.class).getPosition();
                   Vector3i startPos = new Vector3i (originPos.x - 15, originPos.y - 15, originPos.z);
                   Vector3i endPos = new Vector3i (originPos.x + 15, originPos.y + 15, originPos.z);
                   myZone.setStartPosition(startPos);
                   myZone.setEndPosition(endPos);
                   working = true;
               }
               else {
                   logger.info("Failed to find a location!");
               }

               myZone.zonetype = ZoneType.Gather;
               for (EntityRef minionEntity : entityManager.iteratorEntities(MinionComponent.class)){
                    logger.info("Got minion ", minionEntity);
                   minionEntity.getComponent(MinionComponent.class).assignedzone = myZone;
                   minionEntity.getComponent(MinionComponent.class).minionBehaviour = MinionBehaviour.Gather;
               }



        }
    }
}


}
