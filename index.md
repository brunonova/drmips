---
layout: default
---

## About DrMIPS

DrMIPS is a graphical simulator of the [MIPS][MIPS] processor to support computer 
architecture teaching and learning. It is intuitive, versatile and configurable.

The simulator is available not only for personal computers but also for Android 
devices, especially tablets.

DrMIPS was created under the Master's dissertation entitled 
*Tool to Support Computer Architecture Teaching and Learning* at 
[FEUP][FEUP].

DrMIPS is open-source and licensed under the [GPLv3][gpl3], so you are free to 
use, redistribute, modify and improve it (under certain conditions). Feel free
to [contribute][contribute]!

You can find the download links [below](#download).


### Features

*   Free and open-source
*   Available for both PCs and Android devices
*   Can simulate both unicycle and pipeline versions of the MIPS processor
*   Datapath displayed graphically
*   Step-by-step execution and back step
*   Edit registers and data memory during execution
*   Performance mode, where the latencies are simulated and the critical path of
    the processor is shown
*   Highly configurable datapaths and instruction sets, and additional ones can
    be created
*   Custom components can be created externally and then used by the simulator
    at runtime
*   Fairly intuitive and easy to use
*   Integrated code editor, with syntax highlighting and autocomplete in the PC
    version
*   Values can be displayed either in binary, decimal or hexadecimal
*   Light and dark themes
*   The panes of the simulator can be displayed either as tabs or windows in the
    PC version


### Screenshots

{% for type in site.data.screens %}
  <div id="{{ type.id }}">
    {{ type.title }}:<br />
    {% for img in type.images %}
      <a href="images/screens/{{ img.file }}" title="{{ img.desc }}"><img src="images/thumbs/{{ img.file }}" /></a>
    {% endfor %}
  </div>
{% endfor %}


### Papers

*   Bruno Nova, João C. Ferreira and António Araújo,
    ["Tool to Support Computer Architecture Teaching and Learning"][paper_cispee], 
    *1st International Conference of the Portuguese Society for Engineering Education (CISPEE)*, 
    2013.
*   Bruno Nova, António Araújo and João C. Ferreira,
    ["Ferramenta de Apoio ao Ensino e Aprendizagem de Arquitectura de Computadores"][paper_ticai],
    *TICAI 2013-2014: TICs para el Aprendizaje de la Ingeniería*,
    2014.


## Download

Current version: **{{ site.cur_ver }}**

<div id="download_buttons">
  <a id="download_pc" href="{{ site.repourl }}/releases/download/{{ site.cur_ver }}/DrMIPS_{{ site.cur_ver }}.zip">PC version (.zip)</a>
  <a id="download_android" href="{{ site.repourl }}/releases/download/{{ site.cur_ver }}/DrMIPS_{{ site.cur_ver }}.apk">Android version (.apk)</a>
  <a id="download_source" href="{{ site.repourl }}/releases/download/{{ site.cur_ver }}/DrMIPS_{{ site.cur_ver }}.tar.xz">Source code (.tar.xz)</a>
</div>

You can find the changelog and older releases in the project's
[Releases page][releases].

Check [this page][install] for instructions on how to install and run the
simulator.

If you know what PGP is, you can find the signatures of the downloadable files
in the [Releases page][releases], and you can download my PGP key [here][pgp];


### Packages for Linux

There are also packages available for [Ubuntu][ubuntu], [Debian][debian]
and [Arch Linux][arch].
If you are using one of these Linux distributions, or a derivative, you may
want to install these packages instead, and receive automatic updates.
To do so, follow [these instruction][linux].


### Be informed of new releases

You can be notified of the release of new versions of the simulator by
subscribing to the project's [Releases feed][rel_feed].


### Reporting bugs

You can report bugs or ask questions in the project's [Issue tracker][issues].


### Documentation

The documentation of the simulator is provided along with the simulator.
You can also view it [online][documentation].


## License

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

You can read the full license text [here][gpl3].



[paper_cispee]: papers/cispee13_24.pdf "Paper: Tool to Support Computer Architecture Teaching and Learning"
[paper_ticai]: papers/ticai13-14_cap2.pdf "Paper: Ferramenta de Apoio ao Ensino e Aprendizagem de Arquitectura de Computadores"
[pgp]: brunonova.asc "Bruno Nova's PGP key"
[install]: install/ "Installation instructions"
[linux]: linux/ "Linux packages"
[releases]: {{ site.repourl }}/releases "DrMIPS releases"
[rel_feed]: {{ site.repourl }}/releases.atom "DrMIPS releases feed"
[issues]: {{ site.repourl }}/issues "DrMIPS issue tracker"
[contribute]: {{ site.repourl }}/blob/master/CONTRIBUTING.md

[FEUP]: http://www.fe.up.pt/ "Faculdade de Engenharia da Universidade do Porto"
[MIPS]: http://en.wikipedia.org/wiki/MIPS_architecture "MIPS architecture - Wikipedia"
[gpl3]: http://www.gnu.org/licenses/gpl-3.0.html "GNU General Public License v3"
[ubuntu]: http://www.ubuntu.com/ "Ubuntu home page"
[debian]: https://www.debian.org/ "Debian home page"
[arch]: https://www.archlinux.org/ "Arch Linux home page"
[documentation]: https://cdn.rawgit.com/brunonova/drmips/v2.0.1/doc/manuals/index.html "DrMIPS - Documentation"
