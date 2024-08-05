# World Manager

## Useful commands

* `/wm` - Open the main menu
* `/permission (-)[permission]` - Give yourself permission, 
added as a placeholder for actual permission management. Prefix with `-` to revoke.

## Permissions
**Icons for actions will be only visible if the player has the required permission.**

* `worldmanager.command` - Permission to use WorldManager command
* `worldmanager.create` - Permission to create new worlds
* `worldmanager.delete` - Permission to delete unloaded worlds
* `worldmanager.load` - Permission to load unloaded worlds
* `worldmanager.unload` - Permission to unload loaded worlds
* `worldmanager.import` - Permission to import worlds
* `worldmanager.admin` - Permission to invalidate cache
* `worldmanager.*` - Permission to use all WorldManager functions

## Importing worlds
Worlds can be imported from the `templates` subdirectory of the plugin directory. 
Name of the world directory is used for matching.

## Configuration
some aspects of the plugin can be configured in the `config.yml` file in plugin directory.

* `world-unload-check-delay-s` - delay in seconds between checks for worlds to be unloaded (Default: 30 minutes)
* `world-unload-delay-s` - Time in seconds since last visit after which the world is considered for unloading. (Default: 7 days)