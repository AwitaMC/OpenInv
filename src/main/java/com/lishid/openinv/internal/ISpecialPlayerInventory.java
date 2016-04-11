/*
 * Copyright (C) 2011-2014 lishid.  All rights reserved.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation,  version 3.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.lishid.openinv.internal;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public interface ISpecialPlayerInventory {

    public Inventory getBukkitInventory();

    public boolean inventoryRemovalCheck(boolean save);

    public void setPlayerOnline(Player player);

    /**
     * Sets the Player associated with this ISpecialPlayerInventory offline.
     * 
     * @return true if the ISpecialPlayerInventory is eligible for removal
     */
    public boolean setPlayerOffline();
}
