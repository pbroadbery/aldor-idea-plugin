# aldor-idea-plugin
Aldor plugin for intellij

This plugin supports SPAD and Aldor editing with the intellij IDE.

Features:

- Symbol lookup
- Syntax highlighting
- Aldor syntax checks

# Status

- Lookup and highlighting work, at lesat to some extent
- The grammar works on the majority of fricas and aldor source files
- On the fly compilation of aldor files is disabled

# Installation

Take code, build it, add to intellij as a plugin.

Note that as of today, many features are broken/not implemented.

It is expected that an aldor/fricas project consists of a checked out copy of the
source repository.  This is done so that the plugin can invoke make on various targets
instead of having to put together command lines.

# Plans

Version one of this plugin will

- Have decent navigation between domains/categories
- Work with most aldor and fricas source files
- Support on the fly compilation of aldor files
- Show ++ style documentation for aldor and spad domains
