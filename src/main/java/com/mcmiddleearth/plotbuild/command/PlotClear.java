/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mcmiddleearth.plotbuild.command;

import com.mcmiddleearth.plotbuild.plotbuild.Plot;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author Ivan1pl
 */
public class PlotClear extends InsidePlotCommand {
    
    public PlotClear(String... permissionNodes) {
        super(0, true, permissionNodes);
    }
    
    @Override
    protected void execute(CommandSender cs, String... args) {
        Plot plot = checkInPlot((Player) cs);
        if(plot==null) {
            return;
        }
        boolean unclaim=false;
        if(args.length > 0 && args[0].equalsIgnoreCase("-u")) {
            unclaim = true;
        }
        plot.clear(unclaim);
    }
    
}
