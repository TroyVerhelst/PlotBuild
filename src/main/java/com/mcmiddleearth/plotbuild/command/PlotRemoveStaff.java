/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mcmiddleearth.plotbuild.command;

import com.mcmiddleearth.plotbuild.data.PluginData;
import com.mcmiddleearth.plotbuild.plotbuild.PlotBuild;
import com.mcmiddleearth.plotbuild.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author Eriol_Eandur
 */
public class PlotRemoveStaff extends PlotBuildCommand{

    public PlotRemoveStaff(String... permissionNodes) {
        super(1, true, permissionNodes);
        setAdditionalPermissionsEnabled(true);
        setShortDescription(": Adds a player to staff of a plotbuild.");
        setUsageDescription(" <player> [name]: For public projects that want to feature a plotbuild. This command can be used to give the (non-staff) project leader access to the staff commands for the current plotbuild or the plotbuild called [name].");
    }
    
    @Override
    protected void execute(CommandSender cs, String... args) {
        PlotBuild plotbuild = checkPlotBuild((Player) cs, 1, args);
        if(plotbuild == null) {
            return;
        }
        if(!hasPermissionsForPlotBuild((Player) cs, plotbuild)) {
            return;
        }
        OfflinePlayer removedStaff = Bukkit.getOfflinePlayer(args[0]);
        if(removedStaff.getLastPlayed()==0) {
            sendPlayerNotFoundMessage(cs);
            return;
        }
        if(!plotbuild.getStaffList().contains(removedStaff)) {
            sendNotStaffMessage(cs, removedStaff, plotbuild.getName());
            return;
        }
        plotbuild.getStaffList().remove(removedStaff);
        sendRemoveStaffMessage(cs, removedStaff.getName(), plotbuild.getName());
        if(removedStaff.getPlayer()!=cs) {
            sendRemovedStaffPlayerMessage(cs, removedStaff, plotbuild.getName());
        }
        for(OfflinePlayer staff: plotbuild.getStaffList()) {
            if(staff.getPlayer()!=(Player) cs && staff!=removedStaff) {
                sendOtherStaffMessage(cs, staff, removedStaff, plotbuild.getName());
            }
        }
        plotbuild.log(((Player) cs).getName()+" removed "+removedStaff.getName()+" from staff.");
        PluginData.saveData();
    }

    private void sendRemoveStaffMessage(CommandSender cs, String name, String plotbuild) {
        MessageUtil.sendInfoMessage(cs, "You removed "+ name+" from staff of plotbuild "+plotbuild + ".");
    }

    private void sendRemovedStaffPlayerMessage(CommandSender cs, OfflinePlayer newStaff, String name) {
        MessageUtil.sendOfflineMessage(newStaff, "You were removed from staff"
                                                     + " of plotbuild " + name 
                                                     + " by "+ cs.getName()+".");
    }

    private void sendOtherStaffMessage(CommandSender cs, OfflinePlayer staff, OfflinePlayer newStaff, String name) {
        MessageUtil.sendOfflineMessage(staff, cs.getName()+" removed " + newStaff.getName()+ " from staff"
                                                     + " of plotbuild " + name +".");
    }

    private void sendNotStaffMessage(CommandSender cs, OfflinePlayer removedStaff, String name) {
        MessageUtil.sendErrorMessage(cs, removedStaff.getName()+" is not staff of plotbuild "+name + ".");
    }

}
