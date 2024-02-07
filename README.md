# niord-dk [![Build Status](https://travis-ci.com/NiordOrg/niord-dk.svg?branch=master)](https://travis-ci.com/NiordOrg/niord-dk)

# Overview
The niord-dk project contains specific extensions for the danish version of   
[niord](https://github.com/NiordOrg), i.e. a system
for producing and promulgating NW + NM T&P messages.

Niord is documented extensively at http://docs.niord.org

# niord-dk-web

The *niord-web* web-application, found under the [niord](https://github.com/NiordOrg/niord) project,
constitutes the main system for producing and promulgating navigational warnings and notices to 
mariners.

The *niord-dk-web* project is an overlay web-application that customizes *niord-web* for use in
Denmark. 

## Prerequisites

* Java 17
* Maven 3
* MySQL 8.0.32+ (NB: proper spatial support is a requirement)

## Development Set-Up
The easiest way to get started developing on this project is to use Docker.


The [niord-appsrv](https://github.com/NiordOrg/niord-appsrv) project contains scripts for
setting up Wildfly, Keycloak, etc.

However, 

### Starting MySQL and Keycloak

You may want to start by creating a *.env* file in your working directory, which overrides the environment variables 
(such as database passwords) used in the docker compose file.

The following commands will start two MySQL databases, one for the application server 
and one for Keycloak, and also run Keycloak itself.

    mkdir $HOME/.niord-dk
    docker compose up

The initial *mkdir* command is just to avoid permission problems since docker would otherwise create it as owned
by root.

The Keycloak docker image creates an initial domain, "Master", and a Niord user, sysadmin/sysadmin,
that should be used for the initial configuration of the system, whereupon they should be
deleted.

Enter [http://localhost:8080](http://localhost:8080) and check that you can log in using the Niord sysadmin user.

### Finishing touches

Import the Danish test base data into Niord:

    ./dev/install-base-data.sh
    
Within a minute or so, this will import domains, areas, categories, etc. needed to run the Niord-DK project. 
First clean up a bit:
* In Niord, under Sysadmin -> Domains, click the "Create in Keycloak" button for the "NW" and "NM" domains. 
  This will create the two domains in Keycloak. 
* In Keycloak, edit the "Sysadmin" user group. Under "Role Mappings", in the drop-down "Client Roles" select first 
"niord-nw" then "niord-nm" and assign the "sysadmin" client roles to the group.
* While in Keycloak, you may also want to define new user groups for editors and admins, and assign the appropriate 
  client roles for "niord-nw" and "niord-nm" to the groups. 
  Additionally, for admin-related groups (who should be able to manage users in Niord), assign the "manage-clients" and 
  "manage-users" client roles of the "realm-management" client to the groups.
* Delete the "Master" domain in Niord and the corresponding "niord-client-master" client in Keycloak.
* Go through the configuration and settings of the Niord Sysadmin pages and adjust as 
  appropriate.


