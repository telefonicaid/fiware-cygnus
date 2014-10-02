#Chef-based installation

This document shows how to install Cygnus through Chef-based cookbooks and recipes, an automated software deployment tool.

This document does not give details about how to install any of the components of Chef.

##Using Chef-Solo

Having installed [Chef-Solo](http://docs.getchef.com/chef_solo.html) or [Chef Client](http://www.getchef.com/chef/install/) 11.8 or greater (run it in [local mode](http://docs.getchef.com/ctl_chef_client.html#run-in-local-mode)) in your computer, simply download the Cygnus Cookbook (located at `flume/deploymnet/chef/cookbooks`) to `/home/to/chef/cookbooks` and run:

    $ chef-solo -j node.json -c /etc/chef/solo.rb

or:

    $ chef-client --local-mode -j node.json

Where `node.json` is a Json file containing the Chef recipes within the Cygnus Cookbook that must be run:

    {
    	"run_list": [
        	"recipe[cygnus::0.4_install]"
        ]
    }

Please observe there exist both an install and an uninstall recipe for each existing version of Cygnus at the moment of writing this document (`0.1`, `0.2`, `0.2.1`, `0.3` and `0.4`). These <i>main</i> recipes call other sub-recipes within the cookbook.

Cygnus configuration is done through `/home/to/chef/cookbooks/attributes/default.rb`. As you will probably know, those attributes/parameters can be overwritten directly from the Json file (`node.json` in the example). 

##Using Chef Server

TBD.

##Contact information
Francisco Romero Bueno (francisco.romerobueno@telefonica.com)
<br>
Fermín Galán Márquez (fermin.galanmarquez@telefonica.com) 