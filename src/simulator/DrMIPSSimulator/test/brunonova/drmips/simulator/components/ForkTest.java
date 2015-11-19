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

import brunonova.drmips.simulator.Data;
import brunonova.drmips.simulator.exceptions.InvalidCPUException;
import java.util.Arrays;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import static org.junit.Assert.*;

public class ForkTest {
	@Test
	public void testComponent() throws InvalidCPUException, JSONException {
		tComp(32, Arrays.<String>asList(), 2);
		tComp(4, Arrays.asList("out1"), 0);
		tComp(32, Arrays.asList("out1", "out2"), 20000);
		tComp(1, Arrays.asList("out1", "out2", "out3", "out4", "out5"), 1);
	}

	private void tComp(int size, List<String> outIds, int inValue) throws InvalidCPUException, JSONException {
		JSONObject json = new JSONObject().put("x", 0).put("y", 0)
			.put("in", "in").put("size", size)
			.put("out", new JSONArray(outIds));

		Fork c = new Fork("test", json);

		c.getInput().setValue(inValue);
		c.execute();

		Data expectedData = new Data(size, inValue);
		for(String out: outIds) {
			assertEquals(expectedData, c.getOutput(out).getData());
		}
	}
}
