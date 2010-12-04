#!/bin/bash
#
# This file is part of SAMM.
#
# SAMM is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# SAMM is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with SAMM.  If not, see <http://www.gnu.org/licenses/>.
#

#
# ď»żThis file is part of SAMM.
#
# SAMM is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# SAMM is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with SAMM.  If not, see <http://www.gnu.org/licenses/>.
#


CLASSPATH="."
for I in `ls hsqldb-${hsqldb.version}`; do
        CLASSPATH=$CLASSPATH:hsqldb-${hsqldb.version}/$I;
done;
