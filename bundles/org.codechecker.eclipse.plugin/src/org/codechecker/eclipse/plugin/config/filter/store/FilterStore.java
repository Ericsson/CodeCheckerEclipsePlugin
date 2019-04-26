package org.codechecker.eclipse.plugin.config.filter.store;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.LinkedList;

import org.eclipse.core.runtime.IPath;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.codechecker.eclipse.plugin.Activator;
import org.codechecker.eclipse.plugin.config.filter.FilterConfiguration;
import org.codechecker.eclipse.plugin.Logger;
import org.eclipse.core.runtime.IStatus;

public class FilterStore {

    private final String projectName;

    private LinkedList<FilterConfiguration> filterList;

    public FilterStore(String projectName) {
        super();
        this.projectName = projectName;
        this.filterList = new LinkedList<>();
        load();
    }

    public void addFilterConfiguration(FilterConfiguration n) {
        if (filterList.contains(n)) {
            return; //rename or other change
        }
        filterList.add(n);
        save();
    }

    public void removeFilterConfiguration(FilterConfiguration n) {
        filterList.remove(n);
        save();
    }

    public LinkedList<FilterConfiguration> getFilterList() {
        return filterList;
    }

    private void save() {
        //saveFile("global", true); -- TODO!
        saveFile(projectName, false);
    }

    private void load() {
        // loadFile("global", true); -- TODO! handle global configuration
        loadFile(projectName, false);
    }

    public String[] getNames() {
        LinkedList<String> names = new LinkedList<>();

        for (FilterConfiguration fc : filterList) {
            names.add(fc.getName());
        }

        return names.toArray(new String[]{});
    }

    private void loadFile(String name, boolean isGlobal) {
        IPath p = Activator.getPreferencesPath();
        p = p.append("filters-" + name + ".json");
        Gson gson = new Gson();
        File f = p.toFile();

        if (!f.exists()) {
            saveFile(name, isGlobal);
        }

        FileInputStream fis;
        try {
            fis = new FileInputStream(f);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }

            String json = sb.toString();
            LinkedList<FilterConfiguration> subList;

            Type listType = new TypeToken<LinkedList<FilterConfiguration>>() {
            }.getType();

            subList = gson.fromJson(json, listType);

            for (FilterConfiguration fc : subList) {
                filterList.add(fc);
            }

        } catch (Exception e) {
        	Logger.log(IStatus.ERROR, " " + e);
        	Logger.log(IStatus.ERROR, " " + e.getStackTrace());
        }

    }

    private void saveFile(String name, boolean isGlobal) {
        LinkedList<FilterConfiguration> subList = new LinkedList<>();

        for (FilterConfiguration fc : filterList) {
            if (fc.isGloballySaved() == isGlobal) {
                subList.add(fc);
            }
        }

        IPath p = Activator.getPreferencesPath();
        p = p.append("filters-" + name + ".json");
        Gson gson = new Gson();
        File f = p.toFile();
        try {
            FileOutputStream outputStream = new FileOutputStream(f);
            outputStream.write(gson.toJson(subList).getBytes());
            outputStream.close();
        } catch (Exception e) {
        	Logger.log(IStatus.ERROR, " " + e);
        	Logger.log(IStatus.INFO, " " + e.getStackTrace());
        }
    }
}
