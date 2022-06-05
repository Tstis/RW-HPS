/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.game.simulation;

import java.util.ArrayList;
import java.util.HashMap;

public class CollisionEngine {
    public static final byte coll_all = 1;
    public static final byte coll_basic = 3;
    public static final byte coll_basicJumpThough = 4;
    public static final byte coll_box = 13;
    public static final byte coll_button = 35;
    public static final byte coll_characterHeadCheck = 11;
    public static final byte coll_characterOnly = 40;
    public static final byte coll_key = 21;
    public static final byte coll_movable = 60;
    public static final byte coll_noCollide = 2;
    public static final byte coll_tileWater = 52;
    public static final byte coll_unitGround = 45;

    ArrayList<CollisionType> collisionTypeList = new ArrayList<>();
    public CollisionType all = getCollisionType(coll_all);
    public CollisionType noCollide = getCollisionType(coll_noCollide);
    public CollisionType basic = getCollisionType(coll_basic);
    public CollisionType basicJumpThough = getCollisionType(coll_basicJumpThough);
    public CollisionType character = getCollisionType(coll_all);
    public CollisionType characterHeadCheck = getCollisionType(coll_characterHeadCheck);
    public CollisionType box = getCollisionType(coll_box);
    public CollisionType key = getCollisionType(coll_key);
    public CollisionType button = getCollisionType(coll_button);
    public CollisionType characterOnly = getCollisionType(coll_characterOnly);
    public CollisionType unitGround = getCollisionType(coll_unitGround);
    public CollisionType tileWater = getCollisionType(coll_tileWater);
    public CollisionType movable = getCollisionType(coll_movable);

    public CollisionEngine() {
        CollisionType t = this.all;
        t.addCollision(t);
        t.addCollision(getCollisionType(coll_basic));
        t.addCollision(getCollisionType(coll_basicJumpThough));
        t.addCollision(getCollisionType(coll_all));
        t.addCollision(getCollisionType(coll_characterHeadCheck));
        t.addCollision(getCollisionType(coll_box));
        t.addCollision(getCollisionType(coll_key));
        this.unitGround.addCollision(getCollisionType(coll_tileWater));
        CollisionType t2 = this.movable;
        t2.addCollision(t2);
        t2.addCollision(getCollisionType(coll_basic));
        t2.addCollision(getCollisionType(coll_basicJumpThough));
        t2.addCollision(getCollisionType(coll_all));
        t2.addCollision(getCollisionType(coll_characterHeadCheck));
        t2.addCollision(getCollisionType(coll_box));
        t2.addCollision(getCollisionType(coll_key));
        CollisionType t3 = getCollisionType(coll_all);
        t3.addCollision(getCollisionType(coll_basic));
        t3.addCollision(getCollisionType(coll_basicJumpThough));
        t3.addCollision(getCollisionType(coll_all));
        t3.addCollision(getCollisionType(coll_box));
        t3.addCollision(getCollisionType(coll_characterOnly));
        CollisionType t4 = getCollisionType(coll_characterHeadCheck);
        t4.addCollision(getCollisionType(coll_basic));
        t4.addCollision(getCollisionType(coll_all));
        t4.addCollision(getCollisionType(coll_box));
        t4.addCollision(getCollisionType(coll_characterOnly));
        CollisionType t5 = getCollisionType(coll_basic);
        t5.addCollision(getCollisionType(coll_basic));
        t5.addCollision(getCollisionType(coll_basicJumpThough));
        t5.addCollision(getCollisionType(coll_all));
        t5.addCollision(getCollisionType(coll_box));
        CollisionType t6 = getCollisionType(coll_basicJumpThough);
        t6.addCollision(getCollisionType(coll_basic));
        t6.addCollision(getCollisionType(coll_basicJumpThough));
        t6.addCollision(getCollisionType(coll_all));
        t6.addCollision(getCollisionType(coll_box));
        CollisionType t7 = getCollisionType(coll_box);
        t7.addCollision(getCollisionType(coll_basic));
        t7.addCollision(getCollisionType(coll_basicJumpThough));
        t7.addCollision(getCollisionType(coll_all));
        t7.addCollision(getCollisionType(coll_box));
        CollisionType t8 = getCollisionType(coll_key);
        t8.addCollision(getCollisionType(coll_basic));
        t8.addCollision(getCollisionType(coll_basicJumpThough));
        t8.addCollision(getCollisionType(coll_all));
        t8.addCollision(getCollisionType(coll_box));
        CollisionType t9 = this.button;
        t9.addCollision(getCollisionType(coll_all));
        t9.addCollision(getCollisionType(coll_box));
    }

    public CollisionType getCollisionType(byte id) {
        for (CollisionType ct : this.collisionTypeList) {
            if (ct.id == id) {
                return ct;
            }
        }
        CollisionType ret = new CollisionType();
        ret.id = id;
        this.collisionTypeList.add(ret);
        return ret;
    }

    public static class CollisionType {
        HashMap<Byte, CollisionType> collidesWith = new HashMap<>();
        public byte id;

        public boolean getCollision(CollisionType otherId) {
            if (otherId == null) {
                return false;
            }
            return this.collidesWith.containsKey(otherId.id);
        }

        public void addCollision(CollisionType otherId) {
            this.collidesWith.put(otherId.id, otherId);
        }

        public void remove() {
            this.collidesWith.clear();
            this.id = (byte) 0;
        }
    }
}
