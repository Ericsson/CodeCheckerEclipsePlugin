package org.codechecker.eclipse.plugin.config.filter;

import java.util.LinkedList;

import org.codechecker.eclipse.plugin.report.ResultFilter;
import org.codechecker.eclipse.plugin.report.Severity;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

public class FilterConfiguration {

    private String name = "";

    private boolean linkToCurrentEditorByDefalt = true;

    private boolean globallySaved = false;

    private LinkedList<Filter> filters = new LinkedList<Filter>();

    public FilterConfiguration() {
    }

    public FilterConfiguration(String name, boolean linkToCurrentEditorByDefalt,
                               LinkedList<Filter> filters) {
        super();
        this.name = name;
        this.linkToCurrentEditorByDefalt = linkToCurrentEditorByDefalt;
        this.filters = new LinkedList<>();
        for (Filter f : filters) {
            this.filters.add(f.dup());
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isGloballySaved() {
        return globallySaved;
    }

    public void setGloballySaved(boolean globallySaved) {
        this.globallySaved = globallySaved;
    }

    public boolean isLinkToCurrentEditorByDefalt() {
        return linkToCurrentEditorByDefalt;
    }

    public void setLinkToCurrentEditorByDefalt(boolean linkToCurrentEditorByDefalt) {
        this.linkToCurrentEditorByDefalt = linkToCurrentEditorByDefalt;
    }

    public LinkedList<Filter> getFilters() {
        return filters;
    }

    public void setFilters(LinkedList<Filter> filters) {
        this.filters = filters;
    }

    public FilterConfiguration dup() {
        return new FilterConfiguration(name, linkToCurrentEditorByDefalt, filters);
    }

    public ImmutableList<ResultFilter> convertToResultList() {
        ImmutableList.Builder<ResultFilter> builder = new ImmutableList.Builder<>();

        for (Filter f : filters) {
            builder.add(new ResultFilter(f.getFilepath().equals("") ? Optional.<String>absent() :
                    Optional.of(f.getFilepath()), f.getCheckerMsg().equals("") ? Optional
                    .<String>absent() : Optional.of(f.getCheckerMsg()), f.getSeverity(), f
                    .getCheckerId().equals("") ? Optional.<String>absent() : Optional.of(f
                    .getCheckerId()), f.getBuildTarget().equals("") ? Optional.<String>absent() :
                    Optional.of(f.getBuildTarget()), f.isShowSuppressedErrors()));
        }

        return builder.build();
    }

}
