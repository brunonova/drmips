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

public class ShiftLeftTest {
	@Test
	public void testComponent() throws InvalidCPUException {
		tComp(0, 0, 4, 8, 2);
		tComp(0x80, 0x1, 8, 8, 7);
		tComp(0xc0, 0x3, 2, 10, 6);
		tComp(0x80000000, 0x1, 32, 32, 31);
		tComp(0x10, 0x5, 4, 6, 4);
	}

	private void tComp(int expected, int inValue, int inSize, int outSize, int amount) throws InvalidCPUException {
		ShiftLeft c = new ShiftLeft("test", 0, new Point(0, 0), "in", inSize, "out", outSize, amount);
		c.getInput().setValue(inValue);
		c.execute();
		assertEquals(new Data(outSize, expected), c.getOutput().getData());
	}
}
