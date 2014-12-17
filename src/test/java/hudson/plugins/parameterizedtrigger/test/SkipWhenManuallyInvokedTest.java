/*
 * The MIT License
 *
 * Copyright (c) 2004-2010, Sun Microsystems, Inc., Kohsuke Kawaguchi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package hudson.plugins.parameterizedtrigger.test;

import hudson.model.Cause;
import hudson.model.Project;
import hudson.plugins.parameterizedtrigger.*;
import hudson.triggers.SCMTrigger;
import org.jvnet.hudson.test.CaptureEnvironmentBuilder;
import org.jvnet.hudson.test.HudsonTestCase;

import java.util.ArrayList;
import java.util.List;

public class SkipWhenManuallyInvokedTest extends HudsonTestCase {

	/**
	 * Project A: Post-build build-trigger
	 * 			  + currentBuildParameters
	 *			  + no parameters defined
	 *			  + Skip when manually invoked
	 *	=> Project B should be not be triggered
	 *
	 * @throws Exception
	 */
	
	public void testPostBuildTriggerSkipIfManuallyInvokedByUserCause() throws Exception {
		Project<?,?> projectA = createFreeStyleProject("projectA");
		List<AbstractBuildParameters> buildParameters = new ArrayList<AbstractBuildParameters>();
		buildParameters.add(new CurrentBuildParameters());
		projectA.getPublishersList().add(new BuildTrigger(new BuildTriggerConfig("projectB", ResultCondition.SUCCESS, true, true, buildParameters)));
		CaptureEnvironmentBuilder builder = new CaptureEnvironmentBuilder();

		Project<?,?> projectB = createFreeStyleProject("projectB");
		projectB.getBuildersList().add(builder);
		projectB.setQuietPeriod(1);
		hudson.rebuildDependencyGraph();

		projectA.scheduleBuild2(0, new Cause.UserIdCause()).get();
		assertEquals(false, hudson.getQueue().contains(projectB));
		List<String> log = projectA.getLastBuild().getLog(20);
		for (String string : log) {
			System.out.println(string);
		}
 	}

	/**
	 * Project A: Post-build build-trigger
	 * 			  + currentBuildParameters
	 *			  + no parameters defined
	 *			  + Skip when manually invoked = false
	 *			  + cause = SCM Trigger
	 *	=> Project B should be triggered
	 *
	 * @throws Exception
	 */

	public void testPostBuildTriggerExecuteIfSCMTriggerWhileSkipManuallyInvoked() throws Exception {
		Project<?,?> projectA = createFreeStyleProject("projectA");
		List<AbstractBuildParameters> buildParameters = new ArrayList<AbstractBuildParameters>();
		buildParameters.add(new CurrentBuildParameters());
		projectA.getPublishersList().add(new BuildTrigger(new BuildTriggerConfig("projectB", ResultCondition.SUCCESS, true, false, buildParameters)));
		CaptureEnvironmentBuilder builder = new CaptureEnvironmentBuilder();

		Project<?,?> projectB = createFreeStyleProject("projectB");
		projectB.getBuildersList().add(builder);
		projectB.setQuietPeriod(1);
		hudson.rebuildDependencyGraph();

		projectA.scheduleBuild2(0, new SCMTrigger.SCMTriggerCause("UnitTest")).get();
		assertEquals(true, hudson.getQueue().contains(projectB));
		List<String> log = projectA.getLastBuild().getLog(20);
		for (String string : log) {
			System.out.println(string);
		}
	}
	
}