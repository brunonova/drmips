/*
    DrMIPS - Educational MIPS simulator
    Copyright (C) 2013-2015 Bruno Nova <brunomb.nova@gmail.com>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package brunonova.drmips.simulator.components;

import brunonova.drmips.simulator.exceptions.InvalidCPUException;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import static org.junit.Assert.*;

public class OrTest {
	@Test
	public void testComponent() throws InvalidCPUException, JSONException {
		tComp(0, 0, 0);
		tComp(1, 0, 1);
		tComp(1, 1, 0);
		tComp(1, 1, 1);
	}

	private void tComp(int expected, int in1, int in2) throws InvalidCPUException, JSONException {
		JSONObject json = new JSONObject().put("x", 0).put("y", 0)
			.put("in1", "in1").put("in2", "in2").put("out", "out");

		Or c = new Or("test", json);
		c.getInput1().setValue(in1);
		c.getInput2().setValue(in2);
		c.execute();
		assertEquals(expected, c.getOutput().getValue());
	}
}
