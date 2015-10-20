/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mcmiddleearth.plotbuild.plotbuild;

import com.mcmiddleearth.plotbuild.constants.BorderType;
import com.mcmiddleearth.plotbuild.constants.PlotState;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.OfflinePlayer;

/**
 *
 * @author Ivan1pl
 */
public class PlotBuild {
    
    @Getter
    private final String name;
    
    @Getter
    @Setter
    private String info;
    
    @Getter
    @Setter
    private List <Plot> plots = new ArrayList <>();
    
    @Getter
    @Setter
    private List <OfflinePlayer> staffList = new ArrayList <>();
    
    @Getter
    @Setter
    private List <OfflinePlayer> bannedPlayers = new ArrayList <>();
    
    @Getter
    @Setter
    private List <String> history = new ArrayList <>();
    
    @Getter
    @Setter
    private boolean locked = false;
    
    @Getter
    private final boolean priv;
    
    @Getter
    private final boolean cuboid;
    
    @Getter
    private final BorderType borderType;
    
    @Getter
    private final int borderHeight;
    
    public PlotBuild(String name, BorderType borderType, int borderHeight, boolean priv, boolean cuboid) {
        this.name = name;
        this.borderType = borderType;
        this.priv = priv;
        this.cuboid = cuboid;
        this.borderHeight = borderHeight;
    }
    
    public int countUnclaimedPlots() {
        int result = 0;
        for(Plot p : plots) {
            if(p.getState() == PlotState.UNCLAIMED) {
                result++;
            }
        }
        return result;
    }
    
    public boolean hasUnfinishedPlot(OfflinePlayer player) {
        for(Plot plot : plots) {
            if((plot.getState()==PlotState.CLAIMED || plot.getState()==PlotState.REFUSED) && plot.getOwners().contains(player)) {
                return true;
            }
        }
        return false;
    }
     
    private final static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy'-'MM'-'dd' | 'HH':'mm ");

    public void log(String entry) {
        history.add(LocalDateTime.now().format(formatter)+entry);
    }
}
