/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mcmiddleearth.plotbuild.data;

import com.mcmiddleearth.plotbuild.PlotBuildPlugin;
import com.mcmiddleearth.plotbuild.constants.BorderType;
import com.mcmiddleearth.plotbuild.constants.PlotState;
import com.mcmiddleearth.plotbuild.conversations.PlotBuildConversationFactory;
import com.mcmiddleearth.plotbuild.plotbuild.Plot;
import com.mcmiddleearth.plotbuild.plotbuild.PlotBuild;
import com.mcmiddleearth.plotbuild.utils.FileUtil;
import com.mcmiddleearth.plotbuild.utils.ListUtil;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

/**
 *
 * @author Ivan1pl, Eriol_Eandur
 */
public class PluginData {
    
    @Setter
    @Getter
    private static PlotBuildConversationFactory confFactory;
    
    @Getter
    private static final List <PlotBuild> plotbuildsList = new ArrayList <>();
    
    private static final Map <Player, PlotBuild> currentPlotbuild = new LinkedHashMap <>();
    
    private static final Map <Player, Selection> selections = new LinkedHashMap <>();
    
    private static final Map <OfflinePlayer, List<String>> offlineMessages = new LinkedHashMap<>();
    
    @Getter
    private static final Set <String> missingWorlds = new HashSet<>();
    
    @Getter
    private static boolean loaded = false;
    
    private static final File plotBuildDir = new File(PlotBuildPlugin.getPluginInstance().getDataFolder()
                                                    + File.separator + "plotbuilds");
    
    static {
        if(!plotBuildDir.exists()) {
            plotBuildDir.mkdirs();
        }
    }

    public static void setCurrentPlotbuild(Player p, PlotBuild plotbuild) {
        currentPlotbuild.put(p, plotbuild);
    }
    
    public static PlotBuild getCurrentPlotbuild(Player p) {
        return currentPlotbuild.get(p);
    }
    
    public static Selection getCurrentSelection(Player p){
        Selection selection = selections.get(p);
        if(selection == null) {
            selection = new Selection();
            selections.put(p, selection);
        }
        return selection;
    }
    
    public static Plot getPlotAt(Location location) {
        for(PlotBuild plotbuild : plotbuildsList) {
            for(Plot plot : plotbuild.getPlots()) {
                if(plot.isInside(location)) {
                    return plot;
                }
            }
        }
        return null;
    }
    
    public static Plot getIntersectingPlot(Selection selection, boolean cuboid) {
        for(PlotBuild plotbuild : plotbuildsList) {
            for(Plot plot : plotbuild.getPlots()) {
                if(plot.getState()!=PlotState.REMOVED && plot.isIntersecting(selection,cuboid)) {
                    return plot;
                }
            }
        }
        return null;
    }
    
    public static PlotBuild getPlotBuild(String name) {
        for(PlotBuild plotbuild : plotbuildsList) {
            if(plotbuild.getName().equalsIgnoreCase(name)) {
                return plotbuild;
            }
        }
        return null;
    }
    
    public static void addOfflineMessage(OfflinePlayer player, String message) {
        if(offlineMessages.containsKey(player)) {
            List<String> messages = offlineMessages.get(player);
            messages.add(message);
        }
        else {
            List<String> messages = new ArrayList<>();
            messages.add(message);
            offlineMessages.put(player, messages);
        }
    }
    
    public static List<String> getOfflineMessagesFor(Player player) {
        for(OfflinePlayer offline: offlineMessages.keySet()) {
            if(offline!=null){
                Player search = offline.getPlayer();
                if(search==player) {
                    return offlineMessages.get(offline);
                }
            }
        }
        return null;
    }
    
    public static void deleteOfflineMessagesFor(Player player) {
        offlineMessages.remove(player);
    }
    
    public static void saveData() {
        for(PlotBuild plotbuild : plotbuildsList) {
            try {
                savePlotBuild(plotbuild);
            } catch (IOException ex) {
                Logger.getLogger(PluginData.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public static void loadData() {
        FilenameFilter pbFilter = new FilenameFilter() {

            @Override
            public boolean accept(File file, String string) {
                return string.endsWith(".pb");
            }
            
        };
        for(File f : plotBuildDir.listFiles(pbFilter)) {
            try {
                loadPlotBuild(f);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(PluginData.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if(!missingWorlds.isEmpty()) {
            plotbuildsList.clear();
            for(String world : missingWorlds) {
                PlotBuildPlugin.getPluginInstance().getLogger().info(world);
            }
        } else {
            loaded = true;
        }
    }
    
    public static List <MaterialData> getRestoreData(PlotBuild plotbuild, Plot plot) {
        File plotDir = new File(plotBuildDir, plotbuild.getName());
        File plotRestoreData = new File(plotDir, Integer.toString(plotbuild.getPlots().indexOf(plot)) + ".r");
        ArrayList <MaterialData> ret = new ArrayList<>();
        try {
            try (Scanner scanner = new Scanner(plotRestoreData)) {
                scanner.nextLine();
                while(scanner.hasNext()) {
                    Material material = Material.valueOf(scanner.nextLine());
                    byte data = scanner.nextByte();
                    scanner.nextLine();
                    ret.add(new MaterialData(material, data));
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(PluginData.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }
    
    public static boolean deletePlotBuild(PlotBuild plotbuild) {
        File plotBuildFile = new File(plotBuildDir, plotbuild.getName()+".pb");
        File plotDir = new File(plotBuildDir, plotbuild.getName());
        plotbuildsList.remove(plotbuild);
        currentPlotbuild.values().removeAll(Collections.singleton(plotbuild));
        try {
            boolean pbf = plotBuildFile.delete();
Logger.getGlobal().info("PlotbuildFile: "+pbf);
            boolean dr = FileUtil.deleteRecursive(plotDir);
Logger.getGlobal().info("PlotbuildFile: "+dr);
            return pbf && dr;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(PluginData.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
    
    private static void savePlotBuild(PlotBuild plotbuild) throws IOException {
        File plotBuildFile = new File(plotBuildDir, plotbuild.getName()+".pb");
        File plotDir = new File(plotBuildDir, plotbuild.getName());
        plotBuildFile.createNewFile();
        plotDir.mkdir();
        if(plotBuildFile.exists() && plotDir.exists()) {
            FileWriter fw = new FileWriter(plotBuildFile.toString());
            PrintWriter writer = new PrintWriter(fw);
            writer.println(ListUtil.playerListToString(plotbuild.getStaffList()));
            writer.println(ListUtil.playerListToString(plotbuild.getBannedPlayers()));
            writer.println(plotbuild.isLocked());
            writer.println(plotbuild.isPriv());
            writer.println(plotbuild.isCuboid());
            writer.println(plotbuild.getBorderType());
            writer.println(plotbuild.getBorderHeight());
            for(String entry : plotbuild.getHistory()) {
                writer.println(entry);
            }
            writer.close();
            for(int i = 0; i < plotbuild.getPlots().size(); ++i) {
                savePlot(plotbuild.getPlots().get(i), plotDir, i);
            }
        } else {
            throw new IOException();
        }
    }
    
    private static void savePlot(Plot plot, File plotDir, int i) throws IOException {
        File plotDataFile = new File(plotDir, Integer.toString(i) + ".p");
        File plotRestoreFile = new File(plotDir, Integer.toString(i) + ".r");
        boolean saveRestoreData = !plotRestoreFile.exists();
        plotDataFile.createNewFile();
        plotRestoreFile.createNewFile();
        if(plotDataFile.exists() && plotRestoreFile.exists()) {
            FileWriter fw = new FileWriter(plotDataFile.toString());
            PrintWriter writer = new PrintWriter(fw);
            writer.println(plot.getCorner1().getWorld().getName());
            writer.println(plot.getCorner1().getBlockX() + " "
                         + plot.getCorner1().getBlockY() + " "
                         + plot.getCorner1().getBlockZ());
            writer.println(plot.getCorner2().getWorld().getName());
            writer.println(plot.getCorner2().getBlockX() + " "
                         + plot.getCorner2().getBlockY() + " "
                         + plot.getCorner2().getBlockZ());
            writer.println(ListUtil.playerListToString(plot.getOwners()));
            writer.println(plot.getState());
            for(Location l : plot.getBorder()) {
                writer.println(l.getWorld().getName());
                writer.println(l.getBlockX() + " "
                             + l.getBlockY() + " "
                             + l.getBlockZ());
            }
            writer.close();
            if(saveRestoreData) {
                savePlotRestoreData(plot, plotRestoreFile);
            }
        } else {
            throw new IOException();
        }
    }
    
    private static void savePlotRestoreData(Plot plot, File file) throws IOException {
        FileWriter fw = new FileWriter(file.toString());
        PrintWriter writer = new PrintWriter(fw);
        World world = plot.getCorner1().getWorld();
        writer.println(world.getName());
        int miny = 0;
        int maxy = world.getMaxHeight()-1;
        if(plot.getPlotbuild().isCuboid()) {
            miny = plot.getCorner1().getBlockY();
            maxy = plot.getCorner2().getBlockY();
        }
        for(int x = plot.getCorner1().getBlockX(); x <= plot.getCorner2().getBlockX(); ++x) {
            for(int y = miny; y <= maxy; ++y) {
                for(int z = plot.getCorner1().getBlockZ(); z <= plot.getCorner2().getBlockZ(); ++z) {
                    writer.println(world.getBlockAt(x, y, z).getType());
                    writer.println(world.getBlockAt(x, y, z).getData());//getState().getData().toItemStack().getDurability());
                }
            }
        }
        writer.close();
    }
    
    private static void loadPlotBuild(File f) throws FileNotFoundException {
        String name = f.getName();
        name = name.substring(0, name.length()-3);
        Scanner scanner = new Scanner(f);
        List <OfflinePlayer> staffList = ListUtil.playerListFromString(scanner.nextLine());
        List <OfflinePlayer> bannedList = ListUtil.playerListFromString(scanner.nextLine());
        boolean locked = scanner.nextBoolean();
        scanner.nextLine();
        boolean priv = scanner.nextBoolean();
        scanner.nextLine();
        boolean cuboid = scanner.nextBoolean();
        scanner.nextLine();
        BorderType borderType = BorderType.fromString(scanner.nextLine());
        int borderHeight = scanner.nextInt();
        scanner.nextLine();
        ArrayList <String> history = new ArrayList<>();
        while(scanner.hasNext()) {
            history.add(scanner.nextLine());
        }
        scanner.close();
        File plotDir = new File(plotBuildDir, name);
        plotDir.mkdirs();
        List <Plot> plots = loadPlots(plotDir);
        PlotBuild plotbuild = new PlotBuild(name, borderType, borderHeight, priv, cuboid);
        for(Plot p : plots) {
            p.setPlotbuild(plotbuild);
        }
        plotbuild.setLocked(locked);
        plotbuild.setStaffList(staffList);
        plotbuild.setBannedPlayers(bannedList);
        plotbuild.setPlots(plots);
        plotbuild.setHistory(history);
        plotbuildsList.add(plotbuild);
    }
    
    private static List <Plot> loadPlots(File plotDir) throws FileNotFoundException {
        FilenameFilter pFilter = new FilenameFilter() {

            @Override
            public boolean accept(File file, String string) {
                return string.endsWith(".p");
            }
            
        };
        File[] files = plotDir.listFiles(pFilter);
        ArrayList <Plot> plots = new ArrayList<>(Collections.nCopies(files.length, (Plot) null));
        for(File f : files) {
            String name = f.getName();
            name = name.substring(0, name.length()-2);
            int i = Integer.parseInt(name);
            plots.set(i, loadPlot(f));
        }
        return plots;
    }
    
    private static Plot loadPlot(File f) throws FileNotFoundException {
        Scanner scanner = new Scanner(f);
        String worldName1 = scanner.nextLine();
        World world1 = Bukkit.getWorld(worldName1);
        if(world1 == null) {
            missingWorlds.add(worldName1);
        }
        List <Integer> coords1 = ListUtil.integersFromString(scanner.nextLine(), ' ');
        String worldName2 = scanner.nextLine();
        World world2 = Bukkit.getWorld(worldName2);
        if(world2 == null) {
            missingWorlds.add(worldName2);
        }
        List <Integer> coords2 = ListUtil.integersFromString(scanner.nextLine(), ' ');
        Location corner1 = new Location(world1, coords1.get(0), coords1.get(1), coords1.get(2));
        Location corner2 = new Location(world2, coords2.get(0), coords2.get(1), coords2.get(2));
        List <OfflinePlayer> ownersList = ListUtil.playerListFromString(scanner.nextLine());
        PlotState state = PlotState.valueOf(scanner.nextLine());
        List <Location> border = new ArrayList<>();
        while(scanner.hasNext()) {
            String worldName = scanner.nextLine();
            World world = Bukkit.getWorld(worldName);
            if(world == null) {
                missingWorlds.add(worldName);
            }
            List <Integer> coords = ListUtil.integersFromString(scanner.nextLine(), ' ');
            Location location = new Location(world, coords.get(0), coords.get(1), coords.get(2));
            border.add(location);
        }
        return new Plot(corner1, corner2, ownersList, state, border);
    }
}
