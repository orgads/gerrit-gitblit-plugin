# GitBlit plugin

This plugin integrates [GitBlit](https://github.com/gitblit/gitblit) **${GitBlit-Version}** as a repository browser into [Gerrit](https://code.google.com/p/gerrit/),
with full SSO through Gerrit.

* License: [Apache Public License 2.0](http://www.apache.org/licenses/LICENSE-2.0)
* [Home page](https://github.com/tomaswolf/gerrit-gitblit-plugin)
* Installed plugin version: <em id='gerrit-gitblit-current-version'>${pom.version}</em> &mdash; <a id='gerrit-gitblit-version-check' style='display:none;' href='#'>Check for updates</a>

For a list of contributors, see at [GitHub](https://github.com/tomaswolf/gerrit-gitblit-plugin/graphs/contributors).

This is a privately maintained fork of the official Gerrit-Gitblit plugin. Please report any issues with this plugin at the [GitHub issue tracker](https://github.com/tomaswolf/gerrit-gitblit-plugin/issues).

> For the official plugin, see the repository at [Google Code](https://gerrit.googlesource.com/plugins/gitblit/+/master).

# Configuration

There are two different configurations: one for Gerrit so it knows how to generate links that will be processed by the plugin, and
an optional GitBlit configuration for the plugin itself.

## Gerrit configuration

In Gerrit's `gerrit.config`, define the `[gitweb]` section as follows:

	[gitweb]
	        type = custom
	        url = plugins/@PLUGIN@/
	        linkname = browse
	        project = summary/?r=${project}
	        revision = commit/?r=${project}&h=${commit}
	        branch = log/?r=${project}&h=${branch}
	        filehistory = history/?f=\${file}&r=${project}&h=${branch}
	        file = blob/?r=${project}&h=${commit}&f=\${file}
	        roottree = tree/?r=${project}&h=${commit}

This is normally done automatically if you add the plugin and run through `java -jar gerrit.war init -d site_path`, but you can also
add this manually to Gerrit's config file. The `linkname` can be adapted to your taste.

### Configuring the top menu

This plugin adds a "GitBlit" top menu to Gerrit, and also a new sub-menu item to the "Projects" top menu. Since v2.11.162.2 of this plugin, the link
texts for all sub-menu items can be configured to your taste in a `[plugin "@PLUGIN@"]` section in your `gerrit.config`. If the section is not present,
or some values in that section are not defined, the plugin uses built-in default texts. The default configuration would correspond to

	[plugin "@PLUGIN@"]
	        repositories = Repositories
	        activity = Activity
	        documentation = Documentation
	        search =
	        browse = Browse

The first four are sub-menu items of the "GitBlit" top menu, the last one is a new "browse" sub-menu item in Gerrit's "Projects" menu that is shown
for Gerrit's "current" project (since v2.11.162.2).

The "search" sub-menu item is by default not set and will thus not be shown. Setting it makes only sense if you enable GitBlit indexing on some of
your projects. See the [GitBlit documentation](http://gitblit.com/setup_lucene.html) for more information on that.
 
## GitBlit configuration

The plugin includes a minimal default configuration to make GitBlit act only as a repository viewer. You can augment that with further
customizations in [a normal `gitblit.properties`](http://gitblit.com/properties.html) file located in Gerrit's `$GERRIT_SITE/etc` directory.
The built-in configuration, which ensures that GitBlit is configured as a viewer only, always takes precedence. Also, the `git.repositoriesFolder`
property is always set to Gerrit's git directory at `$GERRIT_SITE/git`.

To see the built-in configuration, access it at [`gitblit.properties`](@URL@plugins/@PLUGIN@/static/gitblit.properties).

By default, the built-in configuration does allow anonymous browsing, subject to the repository and ref-level access restrictions defined in Gerrit.
If you want to lock the GitBlit plugin to allow only logged-in users to browse, set in `$GERRIT_SITE/etc/gitblit.properties` the key
`web.authenticateViewPages = true`. This is the only key of the built-in configuration that you _can_ override. 

GitBlit's ticket service, fan-out service, and its plugin mechanism are disabled in this plugin, as is ssh access through GitBlit since Gerrit
already provides that.

The GitBlit `${baseFolder}` is the plugin's data directory provided by Gerrit at `$GERRIT_SITE/data/@PLUGIN@/`.

> Up to and including v2.11.162.1 of this plugin, GitBlit's `${baseFolder}` was at `$GERRIT_SITE/etc/@PLUGIN@/`. If you upgraded from that or an earlier
> version and do have data in `$GERRIT_SITE/etc/@PLUGIN@/`, move it over to the new place. The directory `$GERRIT_SITE/etc/@PLUGIN@/` can then be removed.

# Issue tracking

Report bugs or make feature requests at the [GitHub issue tracker](https://github.com/tomaswolf/gerrit-gitblit-plugin/issues).

<hr style="color: #C0C0C0; background-color: #C0C0C0; border-color: #C0C0C0; height: 2px;" />
<div style="float:right;">
<a href="https://github.com/tomaswolf/gerrit-gitblit-plugin" target="_blank">GitBlit plugin ${pom.version}</a>
</div>

<script type="text/javascript" src="version_check.js"></script>
