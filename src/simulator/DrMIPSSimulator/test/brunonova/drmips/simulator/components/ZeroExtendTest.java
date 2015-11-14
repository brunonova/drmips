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
import brunonova.drmips.simulator.util.Point;
import org.junit.Test;
import static org.junit.Assert.*;

public class ZeroExtendTest {
	@Test
	public void testComponent() throws InvalidCPUException {
		tComp(0xffff, 32, 0xffff, 16, 32);
		tComp(0xf, 4, 0xf, 4, 4);
		tComp(0x7fffffff, 32, 0x7fffffff, 31, 32);
		tComp(0, 32, 0, 1, 32);
	}

	private void tComp(int expectedValue, int expectedSize, int inValue, int inSize, int outSize) throws InvalidCPUException {
		ZeroExtend c = new ZeroExtend("test", 0, new Point(0, 0), "in", inSize, "out", outSize);
		c.getInput().setValue(inValue);
		c.execute();
		assertEquals(new Data(expectedSize, expectedValue), c.getOutput().getData());
	}
}
