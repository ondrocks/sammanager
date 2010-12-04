/**
 * This file is part of SAMM.
 * 
 * SAMM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * SAMM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with SAMM.  If not, see <http://www.gnu.org/licenses/>.
 */
package pl.edu.agh.samm.eclipse.views;

import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.part.ViewPart;

import pl.edu.agh.samm.common.metrics.IConfiguredMetric;
import pl.edu.agh.samm.eclipse.model.IRunningMetricListListener;
import pl.edu.agh.samm.eclipse.model.RunningMetricsList;
import pl.edu.agh.samm.eclipse.views.providers.RunningMetricsContentProvier;

/**
 * @author Pawel Koperek <pkoperek@gmail.com>
 * @author Mateusz Kupisz <mkupisz@gmail.com>
 * 
 */
public class RunningMetricsView extends ViewPart implements IRunningMetricListListener {

	public static String ID = "pl.edu.agh.samm.eclipse.views.RunningMetricsView";
	private ListViewer viewer;

	public RunningMetricsView() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void createPartControl(Composite parent) {
		Composite root = new Composite(parent, SWT.NULL);
		root.setLayout(new FillLayout(SWT.VERTICAL));

		viewer = new ListViewer(root);
		viewer.setContentProvider(new RunningMetricsContentProvier());
		viewer.setInput(RunningMetricsList.getInstance().getList());

		RunningMetricsList.getInstance().addRunningMetricListListener(this);

		getSite().setSelectionProvider(viewer);
	}

	@Override
	public void setFocus() {
		viewer.getList().setFocus();
	}

	@Override
	public void metricAdded(IConfiguredMetric metric) {
		refresh();
	}

	private void refresh() {
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				viewer.refresh();
			}
		});
	}

	@Override
	public void metricRemoved(IConfiguredMetric metric) {
		refresh();
	}

}
