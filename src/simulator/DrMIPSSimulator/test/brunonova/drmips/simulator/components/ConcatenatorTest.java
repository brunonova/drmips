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
import brunonova.drmips.simulator.Data;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import static org.junit.Assert.*;

public class ConcatenatorTest {
	@Test
	public void testComponent() throws InvalidCPUException, JSONException {
		tComp(8, 0x13, 4, 4, 0x1, 0x3);
		tComp(2, 0x2, 1, 1, 0x1, 0x0);
		tComp(32, 0xffff0000, 16, 16, 0xffff, 0x0000);
		tComp(4, 0x0, 3, 1, 0x0, 0x0);
		tComp(32, 0xffffffff, 1, 31, 0x1, 0x7fffffff);
		tComp(32, 0x22222223, 31, 1, 0x11111111, 0x1);
		tComp(3, 0x5, 1, 2, 0x1, 0x1);
	}

	private void tComp(int expectedSize, int expectedValue, int in1Size, int in2Size, int in1Value, int in2Value) throws InvalidCPUException, JSONException {
		JSONObject json = new JSONObject().put("x", 0).put("y", 0)
			.put("in1", new JSONObject().put("id", "in1").put("size", in1Size))
			.put("in2", new JSONObject().put("id", "in2").put("size", in2Size))
			.put("out", "out");

		Concatenator c = new Concatenator("test", json);

		c.getInput1().setValue(in1Value);
		c.getInput2().setValue(in2Value);
		c.execute();

		Data expectedData = new Data(expectedSize, expectedValue);
		assertEquals(expectedData, c.getOutput().getData());
	}
}
