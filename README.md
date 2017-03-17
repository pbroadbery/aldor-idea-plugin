# aldor-idea-plugin
Aldor plugin for intellij

This plugin supports SPAD and Aldor editing with the intellij IDE.

Features:

- Symbol lookup
- Syntax highlighting
- Aldor syntax checks
- On the fly compilation of aldor files

# Status

- Lookup and highlighting work, at least to some extent
- The grammar works on the majority of fricas and aldor source files
- "Goto Class" implemented for Top level definitions and SPAD abbrevs
- "Goto Symbol" implemented for declarations and definitions in domains
- Some variants of rename work (parameters and list comprehension vars at least).
- Documentation for Top level items and declarations

# Installation

Take code, build it, add to intellij as a plugin.

It is expected that an aldor/fricas project consists of a checked out copy of the
source repository.  This is done so that the plugin can invoke make on various targets
instead of having to put together command lines.

# Plans

Version one of this plugin will

- Have decent navigation between domains/categories
- Work with most aldor and fricas source files
- Support on the fly compilation of aldor files
- Show ++ style documentation for aldor and spad domains

# Missing

- Compilation errors are not flagged "nicely", but are visible in the message window
- Some issues with indent sensitive code
- Documentation should have better links

