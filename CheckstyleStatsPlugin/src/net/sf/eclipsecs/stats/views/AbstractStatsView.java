//============================================================================
//
// Copyright (C) 2002-2005  David Schneider, Lars K�dderitzsch, Fabrice Bellingard
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
//============================================================================
package net.sf.eclipsecs.stats.views;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import net.sf.eclipsecs.stats.Messages;
import net.sf.eclipsecs.stats.StatsCheckstylePlugin;
import net.sf.eclipsecs.stats.data.CreateStatsJob;
import net.sf.eclipsecs.stats.data.Stats;
import net.sf.eclipsecs.stats.views.internal.CheckstyleMarkerFilter;
import net.sf.eclipsecs.stats.views.internal.CheckstyleMarkerFilterDialog;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.ide.ResourceUtil;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;
import org.eclipse.ui.progress.WorkbenchJob;

import com.atlassw.tools.eclipse.checkstyle.builder.CheckstyleMarker;

/**
 * Abstract view that gathers common behaviour for the stats views.
 * 
 * @author Fabrice BELLINGARD
 */
public abstract class AbstractStatsView extends ViewPart
{

    //
    // attributes
    //

    /** the main composite. */
    private Composite mMainComposite;

    /** The filter for this stats view. */
    private CheckstyleMarkerFilter mFilter;

    /** The focused resources. */
    private IResource[] mFocusedResources;

    /** The views private set of statistics. */
    private Stats mStats;

    /** The listener reacting to selection changes in the workspace. */
    private ISelectionListener mFocusListener;

    /** The listener reacting on resource changes. */
    private IResourceChangeListener mResourceListener;

    //
    // methods
    //

    /**
     * {@inheritDoc}
     * 
     * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    public void createPartControl(Composite parent)
    {

        mMainComposite = parent;

        // create and register the workspace focus listener
        mFocusListener = new ISelectionListener()
        {
            public void selectionChanged(IWorkbenchPart part,
                ISelection selection)
            {
                AbstractStatsView.this.focusSelectionChanged(part, selection);
            }
        };

        getSite().getPage().addSelectionListener(mFocusListener);
        focusSelectionChanged(getSite().getPage().getActivePart(), getSite()
            .getPage().getSelection());

        // create and register the listener for resource changes
        mResourceListener = new IResourceChangeListener()
        {
            public void resourceChanged(IResourceChangeEvent event)
            {

                IMarkerDelta[] markerDeltas = event.findMarkerDeltas(
                    CheckstyleMarker.MARKER_ID, true);

                if (markerDeltas.length > 0)
                {
                    refresh();
                }
            }
        };

        ResourcesPlugin.getWorkspace().addResourceChangeListener(
            mResourceListener);

        makeActions();
        initActionBars(getViewSite().getActionBars());
    }

    /**
     * {@inheritDoc}
     */
    public void setFocus()
    {

    }

    /**
     * {@inheritDoc}
     */
    public void dispose()
    {
        // IMPORTANT: Deregister listeners
        getSite().getPage().removeSelectionListener(mFocusListener);
        ResourcesPlugin.getWorkspace().removeResourceChangeListener(
            mResourceListener);

        super.dispose();
    }

    /**
     * Opens the filters dialog for the specific stats view.
     */
    public final void openFiltersDialog()
    {

        CheckstyleMarkerFilterDialog dialog = new CheckstyleMarkerFilterDialog(
            mMainComposite.getShell(), (CheckstyleMarkerFilter) getFilter()
                .clone());

        if (dialog.open() == Window.OK)
        {
            CheckstyleMarkerFilter filter = dialog.getFilter();
            filter.saveState(getDialogSettings());

            mFilter = filter;
            refresh();
        }
    }

    /**
     * Initializes the action bars of this view.
     * 
     * @param actionBars
     *            the action bars
     */
    protected void initActionBars(IActionBars actionBars)
    {
        initMenu(actionBars.getMenuManager());
        initToolBar(actionBars.getToolBarManager());
    }

    protected abstract void initMenu(IMenuManager menu);

    protected abstract void initToolBar(IToolBarManager tbm);

    /**
     * Returns the filter of this view.
     * 
     * @return the filter
     */
    protected final CheckstyleMarkerFilter getFilter()
    {
        if (mFilter == null)
        {
            mFilter = new CheckstyleMarkerFilter();
            mFilter.restoreState(getDialogSettings());
        }

        return mFilter;
    }

    /**
     * Returns the statistics data.
     * 
     * @return the data of this view
     */
    protected final Stats getStats()
    {
        return mStats;
    }

    /**
     * Causes the view to re-sync its contents with the workspace. Note that
     * changes will be scheduled in a background job, and may not take effect
     * immediately.
     */
    protected final void refresh()
    {

        IWorkbenchSiteProgressService service = (IWorkbenchSiteProgressService) getSite()
            .getAdapter(IWorkbenchSiteProgressService.class);

        // rebuild statistics data
        CreateStatsJob job = new CreateStatsJob(getFilter());
        job.setRule(ResourcesPlugin.getWorkspace().getRoot());
        job.addJobChangeListener(new JobChangeAdapter()
        {
            public void done(IJobChangeEvent event)
            {
                mStats = ((CreateStatsJob) event.getJob()).getStats();
                Job uiJob = new WorkbenchJob(Messages.AbstractStatsView_msgRefreshStats)
                {

                    public IStatus runInUIThread(IProgressMonitor monitor)
                    {
                        handleStatsRebuilt();
                        return Status.OK_STATUS;
                    }
                };
                uiJob.setPriority(Job.INTERACTIVE);
                uiJob.setSystem(true);
                uiJob.schedule();
            }
        });

        service.schedule(job, 0, true);
    }

    /**
     * Returns the dialog settings for this view.
     * 
     * @return the dialog settings
     */
    protected final IDialogSettings getDialogSettings()
    {

        String concreteViewId = getViewId();

        IDialogSettings workbenchSettings = StatsCheckstylePlugin.getDefault()
            .getDialogSettings();
        IDialogSettings settings = workbenchSettings.getSection(concreteViewId);

        if (settings == null)
        {
            settings = workbenchSettings.addNewSection(concreteViewId);
        }

        return settings;
    }

    /**
     * Returns the view id of the concrete view. This is used to make separate
     * filter settings (stored in dialog settings) for different concrete views
     * possible.
     * 
     * @return the view id
     */
    protected abstract String getViewId();

    /**
     * Callback for subclasses to refresh the content of their controls, since
     * the statistics data has been updated.<br/>Note that the subclass should
     * check if their controls have been disposed, since this method is called
     * by a job that might run even if the view has been closed.
     */
    protected abstract void handleStatsRebuilt();

    /**
     * Create the viewer actions.
     */
    protected abstract void makeActions();

    /**
     * Invoked on selection changes within the workspace.
     * 
     * @param part
     *            the workbench part the selection occurred
     * @param selection
     *            the selection
     */
    private void focusSelectionChanged(IWorkbenchPart part, ISelection selection)
    {

        List resources = new ArrayList();
        if (part instanceof IEditorPart)
        {
            IEditorPart editor = (IEditorPart) part;
            IFile file = ResourceUtil.getFile(editor.getEditorInput());
            if (file != null)
            {
                resources.add(file);
            }
        }
        else
        {
            if (selection instanceof IStructuredSelection)
            {
                for (Iterator iterator = ((IStructuredSelection) selection)
                    .iterator(); iterator.hasNext();)
                {
                    Object object = iterator.next();
                    if (object instanceof IAdaptable)
                    {
                        IResource resource = (IResource) ((IAdaptable) object)
                            .getAdapter(IResource.class);

                        if (resource == null)
                        {
                            resource = (IResource) ((IAdaptable) object)
                                .getAdapter(IFile.class);
                        }

                        if (resource != null)
                        {
                            resources.add(resource);
                        }
                    }
                }
            }
        }

        IResource[] focusedResources = new IResource[resources.size()];
        resources.toArray(focusedResources);

        // check if update necessary -> if so then update
        boolean updateNeeded = updateNeeded(mFocusedResources, focusedResources);
        if (updateNeeded)
        {
            mFocusedResources = focusedResources;
            getFilter().setFocusResource(focusedResources);
            refresh();
        }
    }

    /**
     * Checks if an update of the statistics data is needed, based on the
     * current and previously selected resources. The current filter setting is
     * also taken into consideration.
     * 
     * @param oldResources
     *            the previously selected resources.
     * @param newResources
     *            the currently selected resources
     * @return <code>true</code> if an update of the statistics data is needed
     */
    private boolean updateNeeded(IResource[] oldResources,
        IResource[] newResources)
    {
        // determine if an update if refiltering is required
        CheckstyleMarkerFilter filter = getFilter();
        if (!filter.isEnabled())
        {
            return false;
        }

        int onResource = filter.getOnResource();
        if (onResource == CheckstyleMarkerFilter.ON_ANY_RESOURCE
            || onResource == CheckstyleMarkerFilter.ON_WORKING_SET)
        {
            return false;
        }
        if (newResources == null || newResources.length < 1)
        {
            return false;
        }
        if (oldResources == null || oldResources.length < 1)
        {
            return true;
        }
        if (Arrays.equals(oldResources, newResources))
        {
            return false;
        }
        if (onResource == CheckstyleMarkerFilter.ON_ANY_RESOURCE_OF_SAME_PROJECT)
        {
            Collection oldProjects = CheckstyleMarkerFilter
                .getProjectsAsCollection(oldResources);
            Collection newProjects = CheckstyleMarkerFilter
                .getProjectsAsCollection(newResources);

            if (oldProjects.size() == newProjects.size())
            {
                return !newProjects.containsAll(oldProjects);
            }
            else
            {
                return true;
            }
        }

        return true;
    }

}