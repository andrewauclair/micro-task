# micro-task Design Document

This document defines the design for micro-task, a command line interface task tracker. micro-task has been designed to
track developer tasks in great detail. The developer can group their tasks into lists and groups, they can define
projects with features and milestones to further group their tasks. With these projects, features and milestones they can
define when their tasks are due and ensure that they are working on the most pressing data.

What follows is a detailed design of micro-task and its many features and commands.


## 1. Considerations
#### 1.1 Assumptions
micro-task requires a Java VM of at least Java 14 installed.

#### 1.2 System Environment
Currently, micro-task is only designed for Windows 10.

## 2. Architecture

##### 2.1 Commands

Commands can be found in the com.andrewauclair.microtask.command package. For the most part each command is in its own Java file.
Some subcommands are broken out into their own classes due to their complexity.
 
##### 2.2 Data Manipulation

The com.andrewauclair.microtask.task package contains classes responsible for manipulating data related to tasks.

TODO - Define how and where we manipulate projects, features and milestones when they are developed.

##### 2.3 Data Structures

TODO - Define an item here for each of the structures that we have (task, list, group, project, feature, milestone).

###### 2.3.1 Task
<!---

groups, lists, tasks, projects, features, milestones
commands layout

#### 2.1 Overview
*Provide here a descriptive overview of the software/system/application architecture.*

#### 2.2 Component Diagrams
*Provide here the diagram and a detailed description of its most valuable parts. There may be multiple diagrams. Include a description for each diagram. Subsections can be used to list components and their descriptions.*

#### 2.3 Class Diagrams
*Provide here any class diagrams needed to illustrate the application. These can be ordered by which component they construct or contribute to. If there is any ambiguity in the diagram or if any piece needs more description provide it here as well in a subsection.*

#### 2.4 Sequence Diagrams
*Provide here any sequence diagrams. If possible list the use case they contribute to or solve. Provide descriptions if possible.*

#### 2.5 Deployment Diagrams
*Provide here the deployment diagram for the system including any information needed to describe it. Also, include any information needed to describe future scaling of the system.*

#### 2.6 Other Diagrams
*Provide here any additional diagrams and their descriptions in subsections.*
--->

## 3. User Experience

#### 3.1 Adding New Data

The primary focus of micro-task, and the first thing a user will do, is add new data. This might be done by adding a new list,
group, project, feature or task. All of these operations should be simple and quick to execute. Adding new data should be the data
instantly accessible to the user.

The following sections define what happens when the developer adds each type of data and how to do it.

##### 3.1.1 Task

The most basic piece of data to track in micro-task is a task. When entering a new task, the task is added
to the active context list or to the list specified with the command options.

A task can be defined with the `add` command.

##### 3.1.2 Group

A group is a collection of lists and other groups represented by a folder in the file system and contains a group.txt file
which defines its state. Groups might also contain a project.txt file, defining that the group is a project.

##### 3.1.3 List

A list is a collection of tasks, also represented by a folder in the file system and containing a list.txt file
which defines its state. Lists might also contain a feature.txt file, defining that the list is a feature. The list can
only be a feature if its parent group is a project.

##### 3.1.4 Project

A project is a collection of specific features and tasks that need to be completed for a project.

When the user creates a project, a number of files will be created. First, a new group will be added in the `/projects/` group.
This new group will be named after the project and will contain the features of the project.


A project can be defined with the `add` command.

##### 3.1.5 Feature

A feature is a way to define a set of tasks that belong to the same component of a project. These can be as narrow or broad as the user
wishes them to be. micro-task imposes no limits on the number of features created, nor the number of tasks assigned to a feature.

Features will be automatically created from groups and lists in a project group.

##### 3.1.6 Milestone

A milestone defines a set of features that will be completed together. Usually this maps to a real world milestone with a due date. micro-task
provides the user with a way to define a due date for a milestone and will use this due date in display data.

A milestone can be defined with the `add` command.

##### 3.1.7 Tags

Tags separate data on lists. This helps the user by requiring fewer lists to make sets of tasks unique. Tags can be set in the active
context to help further filter the active tasks. Tags are defined by the user and can be set to any string of text.

Unless tags are specified when adding a task, the active context tags will be used. This allows the user to quickly add tasks
with tags.


TODO - Something might have to be done about how to switch the active context tags easier.


#### 3.2 Working On Tasks

##### 3.2.1 Outside of Projects

Not all tasks need to be assigned to features of a project. The user is free to create groups and lists of tasks that don't belong
to any projects. When working on these tasks, there is no active project, milestone or feature in the active context.

##### 3.2.2 On Projects

The user has several approaches to working on a project. They can start working on a feature or milestone specifically with the `start` command or
they can start the project its self and see all of the tasks for that project.

The behavior of the `tasks` command changes based on the active context. In the case of working on projects
this means that if the user has decided to start the project with the `start` command, `tasks` will display all of the tasks for that project.
If they have used the milestone or feature in the `start` command, only the tasks for those will be displayed, respectively.

## 4. File Structure

##### 4.1 Task

micro-task stores task data in a text file defined by its task ID. For example, task 50 is in 50.txt. This file can be found in the folder belonging to the list that the task is on.

<b>Format (line by line):</b>

| Line | Description |
| ---- | ----------- |
| `name <text>` | a string defining the name of the task. |
| `state <enum>` | One of `Inactive`, `Active`, `Finished` |
| `recurring <bool>` | Either `true` or `false`, indicating <br>that the task is recurring and there for <br>cannot be finished. |
| `due <date>` | The due date for this milestone. <br>This is optional, if not set by the user then the line will read `due none`. |
| `<blank>` | |
| `tag <tag>` | Any number of tags can be defined here. |
| `<blank>` | |
| `note <time> <text>` | Any number of notes can be defined here. |
| `<blank>` | |
| `custom` | |
| `<type>` `<value>` | Used for storing custom values defined by the user. |
| `end-custom` | |
| `add <time>` | The time at which the task was added. |
| | Start and stop are repeated together as many<br> times as the task is worked on. |
| `start <time>` | The time that the task was started.<br>  |
| `stop <time>` | The time that the task was stopped. |
| `finish <time>` | The time that the task was finished. |

##### 4.2 List

A list is a folder with its given name. For example, the list /default uses the default folder. Inside of this list folder there is any number of task files (task-<id>.txt), and a list.txt file that designates the folder as a list and defines list specific information.

Lists contain a flag to indicator they are a feature. This is done so that the user can have non-feature lists inside of a project.

<b>Format (line by line):</b>

| Line | Description |
| ---- | ----------- |
| `state <enum>` | `InProgress` or `Finished` |
| `note <time> <text>` | Any number of notes related to the list. |

##### 4.3 Group

A group is a folder with its given name, just like a list. For example, the group /projects/ is stored in the projects folder.
		Inside of this group folder there is any number of list folders and a group.txt file that designates the folder as a group and defines
		group specific information, similar to lists.

Groups contain a flag to indicator they are a feature. This is done so that the user can have non-feature groups inside of a project.

<b>Format (line by line):</b>

| Line | Description |
| ---- | ----------- |
| `state <enum>` | `InProgress` or `Finished` |
| `note <time> <text>` | Any number of notes related to the group. |

##### 4.4 Project

A project is a special form of group. Project folders will be defined the same as group folders with an additional file, project.txt.
		The project.txt file defines a more detailed name for the project along with other information, such as any number of notes added
		by the user.
		
<b>Format (line by line):</b>

| Line | Description |
| ---- | ----------- |
| `name <text>` | Name of the project. This is more descriptive than the short name used for the file name. |
| `due <date>` | The due date for this milestone. <br>This is optional, if not set by the user then the line will read `due none`. |

##### 4.5 Feature

A feature is a special version of either a group or list folder. A feature starts out as a list, but can be converted to a group
		if a sub-feature is added. A special feature.txt file is added to the group or list folder to give further details on the feature
		and to indicate that the group or list is a feature.

Features will be automatically created when groups or lists are created inside a project group. If a project is created
from an existing group then all groups and lists in the project group will be turned into features.

<b>Format (line by line):</b>

| Line | Description |
| ---- | ----------- |
| `name <text>` | Name of the feature. This is more descriptive than the short name used for the file name. |
| `due <date>` | The due date for this milestone. <br>This is optional, if not set by the user then the line will read `due none`. |

##### 4.6 Milestone

Milestones are stored in a special milestones folder in the project group folder. For example /projects/<project>/milestones. This
		milestones folder is neither a list, nor a group. Instead the milestones folder is special and milestones is a reserved keyword that
		cannot be used as a list or group name. In this folder are milestone files, these are named like so: milestone-<id>.txt. These files
		contain data relating to the milestone. For now this data is just the features that belong to the milestone, a description and an optional
		due date.

<b>Format (line by line):</b>

| Line | Description |
| ---- | ----------- |
| `state <enum>` | `InProgress` or `Finished` |
| `due <date>` | The due date for this milestone. <br>This is optional, if not set by the user then the line will read `due none`. |
| `note <time> <text>` | Any number of notes related to the milestone. |

##### 4.7 Archive

With the data defined above, it would be easy to write a ton of folders and files to the file system. Only the active data
will ever be modified, and the inactive data wastes space. Space can be saved by archiving inactive data into large files
stored elsewhere in the file system. Data will be archived automatically when finishing a project and requires no thought or 
interaction from the user. Below is the format for an archive file.

File name `project-<id>.txt`

| Line | Description |
| `file <path>` | The name of the file whose contains are to follow. |
| `<file contents>` | The contents of the file. |

## 5. Command Line Interface
micro-task is designed to run standalone from the command line or as an interactive program that continuously takes input.
When running in the interactive mode the user can see additional status data.

#### 5.X Standalone Mode

In the standalone mode micro-task commands take the form `micro-task add task -n "Test"`.

In this mode all commands have a `--json` option that can be used to output data in json instead of the normal
format that is used when in the interactive mode, which is more of a pretty print for the user to look at. `--json`
makes writing scripts that use micro-task easier.
 
#### 5.X Add Command

##### Sub-Commands
| Sub-Command | Description |
| ----------- | ----------- |
| `task` | Add a new task |
| `list` | Add a new list |
| `group` | Add a new group |
| `project` | Add a new project |
| `feature` | Add a new feature |
| `milestone` | Add a new milestone |

##### Options

| Option | Sub-Command(s) | Description |
| ------ | -------------- | ----------- |
| `-h`,`--help` | All | Print the help message for this command. |
| `-n`,`--name` | All | The name of the new task, list, group, project, feature or milestone. |
| `-l`,`--list` | `task` | The list to add the task to. Defaults to the `active context list` |
| `-g`,`--group` | `list`, `group` | The group to add the list or group to. Defaults to the `active context group` |


##### Error Messages

- Cannot add a task to finished list.
- Cannot add list to finished group.
- Cannot add group to finished group.

#### 5.X Start Command

The start command allows the user to start a task, list, group, project, feature or milestone.

Active tasks act independently of any other active context item. Setting the active list, group, project or feature
will not change the active task.

When starting a list the active group will be cleared. When starting a group the active list will be cleared. When starting
a project the active list and group will be cleared.

Starting a feature will set the active project to that feature's project.

Starting a milestone will set the active project to that milestone's project.

The active project, feature and milestone are also independent like the active task. When starting a list or group
the active project, feature and milestone will not change. This makes it convenient to work outside of the project briefly
without having to set up the active context again.

##### Sub-Commands
| Sub-Command | Description |
| ----------- | ----------- |
| `task` | Start a task |
| `list` | Start a list |
| `group` | Start a group |
| `project` | Start a project |
| `feature` | Start a feature |
| `milestone` | Start a milestone |

##### Options
| Option | Sub-Command(s) | Description |
| ------ | -------------- | ----------- |
| `-h`,`--help` | All | Print the help message for this command. |
| `-f`,`--finish` | `task` | Finish the previous task when starting another. |

#### 5.X Stop Command

The stop command allows the user to stop the active task, list or group.

#### 5.X Finish Command

The finish command allows the user to finish a task, list, group, project, feature or milestone.

#### 5.X Change Command

The change command allows the user to switch the current list or group. This allows for some easier navigation
when adding new lists, groups or tasks instead of specifying the list or group in every command.

##### Options
| Sub-Command | Descrption |
| ----------- | ---------- |
| `list`      | List to switch to |
| `group`     | Group to switch to |

| Option | Sub-Command(s) | Description |
| ------ | -------------- | ----------- |
| `-h`,`--help` | All | Print the message for this command. |

#### 5.X Move Command

The move command allows the user to move tasks, lists, groups.

#### 5.X Search Command

The search command provides the user a way to find tasks that they are looking for. This command can be used to search
the current list, another list or another group entirely.

#### 5.X Tasks Command

The task command displays tasks from the active context. This command can also be used to display tasks
from other sources not specified by the active context.

Type ID Description (normal mode)
Type ID Project Feature Milestone Description (project mode)

#### 5.X Times Command

The times command allows the user to display time related data. This can be a simple command to display time spent
in the current day, to a specific display of times for filtered data from months ago. This is perhaps the most
complex command in all of micro-task.

#### 5.X Update Command

The update command allows the user to update the application and their local task repo.

##### Sub-Commands
| Sub-Command | Description |
| ----------- | ----------- |
| `app` | Update the application with releases found on GitLab |
| `repo` | Update the local repo with remote data, or update the remote repo with local data |

##### Options

| Option | Sub-Command | Description |
| ------ | -------------- | ----------- |
| `-h`,`--help` | All | Print the help message for this command. |
| `-n`,`--name` | All | The name of the new task, list, group, project, feature or milestone. |
| `-l`,`--list` | `task` | The list to add the task to. Defaults to the `active context list` |
| `-g`,`--group` | `list`, `group` | The group to add the list or group to. Defaults to the `active context group` |


##### Error Messages

- Finishing group when not all lists have been finished
- finishing list when not all tasks have been finished

<!---
## 4 Appendices and References


#### 4.1 Definitions and Abbreviations
*List here any definitions or abbreviations that could be used to help a new team member understand any jargon that is frequently referenced in the design document.*

#### 4.2 References
*List here any references that can be used to give extra information on a topic found in the design document. These references can be referred to using superscript in the rest of the document.*

--->