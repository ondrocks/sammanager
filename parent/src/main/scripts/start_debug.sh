#!/bin/bash
#
# This file is part of SAMM.
#
# SAMM is free software: you can redistribute it and/or modify
# it undert he terms of the GNU General Public License as published by
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

CODEBASE="file://$(pwd)/core-0.1-SNAPSHOT.jar file://$(pwd)/api-0.1-SNAPSHOT.jar"
DEBUG_OPTS="-Xdebug -Xrunjdwp:transport=dt_socket,address=9999,server=y"

java $DEBUG_OPTS -DconfigFile="$1" -Dosgi.compatibility.bootdelegation=true -Djava.security.manager=java.rmi.RMISecurityManager -Djava.security.policy=server.policy -Djava.rmi.server.codebase="$CODEBASE" -jar osgi/org.eclipse.osgi-3.6.0.v20100517.jar -console
