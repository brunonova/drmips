---
layout: default
title: Linux packages
---

## Ubuntu

Packages for Ubuntu and derivatives are available in the official repositories
and also in a PPA.

##### Install from official Ubuntu repositories:

First, [enable the "backports" repository][ubuntu_backports] and the
"universe" component if you haven't done so already.
Enabling the "backports" repository is not necessary to install DrMIPS in
Ubuntu 15.04 Vivid Vervet or later, but it's needed to receive the latest
updates.

Then install the **drmips** package using the software center, package manager
or terminal.

**Note:** due to the Ubuntu update policy, you won't receive newer versions of
drmips, unless you enable the backports repository. But even if you do,
updates can take a long time to reach the repository and you'll probably have to
upgrade the package manually.
If you want to receive updates faster, you can install DrMIPS from a PPA.

##### Install from a PPA:

You can find the package in this PPA: [ppa:brunonova/ppa][ubuntu_ppa].
New DrMIPS versions will be available faster in the PPA than in the official
Ubuntu repositories.

To install it, follow these instructions:

1.  Open the Ubuntu Software Center (or a package manager like Synaptic).
2.  Click **Edit > Software Sources** (or similar) in the menu.
3.  Click **Add** in **Other Software** tab and enter `ppa:brunonova/ppa` to add
    the PPA.
4.  Close the dialog. Then search for **drmips** in the Software Center and
    install it.
    If it doesn't appear, wait a minute or two and search again.

Or, if you prefer using the command-line, open a terminal and execute the
following commands:

    sudo add-apt-repository ppa:brunonova/ppa
    sudo apt-get update
    sudo apt-get install drmips

##### Unity global menu integration:

Unity - the default desktop environment of Ubuntu - integrates the menus of the
windows with the top/title bar (global menu). However, Java applications
(like DrMIPS) aren't supported by default.

To fix this, install [jayatana][jayatana] by following
[these instructions][jayatana_install].


## Debian

Packages for Debian and derivatives are available in the official repositories.

##### Debian Stable:

[Enable the "backports" repository][debian_backports] and the "contrib"
component if you haven't done so already.
Enabling the "backports" repository is not necessary to install DrMIPS in
Debian 8 Jessie or later, but it's needed to receive the latest
updates.

Then install the **drmips** package using the package manager or
terminal.

**Note:** due to the Debian update policy, you won't receive newer versions of
drmips, unless you enable the backports repository. But even if you do,
updates can take a long time to reach the repository and you'll probably have to
upgrade the package manually. 

##### Debian unstable and testing:

Simply install the **drmips** package from the official repositories using the
package manager or terminal.
You may need to enable the "contrib" component.


## Arch Linux

Packages for Arch Linux and derivatives are available in the Arch User Repository.

##### Install using Yaourt:

If you have [Yaourt][yaourt] installed, open a terminal and execute this
command:

    yaourt drmips

Then, select the **drmips** package and follow the instructions.
If you also want the manuals installed, select the **drmips-doc** package as
well.

##### Install manually:

Follow [these instructions][aur_inst] to install the [drmips][drmips_aur]
package and, optionally, [drmips-doc][drmips-doc_aur], which includes the
manuals.



[ubuntu_backports]: https://help.ubuntu.com/community/UbuntuBackports "UbuntuBackports - Ubuntu Wiki"
[ubuntu_ppa]: https://launchpad.net/~brunonova/+archive/ubuntu/ppa "Bruno Nova's PPA"
[jayatana]: https://launchpad.net/~danjaredg/+archive/ubuntu/jayatana "Java Ayatana Launchpad page"
[jayatana_install]: http://www.webupd8.org/2014/02/get-unity-global-menu-hud-support-for.html "Get Unity Global Menu / HUD Support For Java Swing Applications With JAyatana ~ Web Upd8"
[debian_backports]: http://backports.debian.org/Instructions/ "Instructions - Debian Backports"
[yaourt]: https://wiki.archlinux.org/index.php/yaourt "Yaourt - ArchWiki"
[aur_inst]: https://wiki.archlinux.org/index.php/Arch_User_Repository#Installing_packages "Arch User Repository (Installing packages) - Arch Wiki"
[drmips_aur]: https://aur.archlinux.org/packages/drmips/ "drmips - AUR"
[drmips-doc_aur]: https://aur.archlinux.org/packages/drmips-doc/ "drmips-doc - AUR"
