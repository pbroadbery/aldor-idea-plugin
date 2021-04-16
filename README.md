# aldor-idea-plugin
Aldor plugin for intellij

This plugin supports SPAD and Aldor editing with the intellij IDE.

Features:

- Symbol lookup
- Syntax highlighting
- Aldor syntax checks
- Browsing type hierarchy

# Status

- Syntax Highlighting works
- Type Lookup works 
- The grammar works on the majority of fricas and aldor source files
- On the fly compilation of aldor files is disabled

# Installation

Please use the latest release of the plugin - See under releases.
Download the .zip file and use the Plugins section of File/Settings to install a plugin from disk.
This version will support any version later than 2020.1

# Current state

Version 1.3.2 of this plugin:

- Decent navigation between domains/categories
- Work with most aldor and fricas source files
- Show ++ style documentation for aldor and spad domains
- Aldor on the fly compilation has been disabled

- Download from [Release-1.3.2](https://github.com/pbroadbery/aldor-idea-plugin/releases/tag/release-1.3.2)

# Plans

- Support on the fly compilation of aldor files
- Support editing files in clones of the fricas and aldor git repos
- Improved hierarchy views 'what implements this domain'
- Show spad documentation as per http://fricas.github.io
- Gutter indicators for 'goto declaration' & 'goto implementations'
- Error recovery on .spad files 
