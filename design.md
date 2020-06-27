# micro-task Design Document


## 1. Considerations


#### 1.1 Assumptions
micro-task requires a Java VM of at least Java 14 installed.

#### 1.2 Constraints
*In this section describe any constraints on the system that have a significant impact on the design of the system.*

#### 1.3 System Environment
Currently micro-task is only designed for Windows 10.

## 2. Architecture

##### 2.1 Commands

Commands can be found in the com.andrewauclair.microtask.command package. For the most part each command is in its own Java file.
Some subcommands are broken out into their own classes due to their complexity.
 
##### 2.2 Data Manipulation

The com.andrewauclair.microtask.task package contains a large number of tasks responsible for manipulating data related to tasks.

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

##### 3.1.1 Task

A new task can be added using the add command (add task).

##### 3.1.2 Group (historically this has been "mk -g <group>", but maybe be move to "add -g <group>")
##### 3.1.3 List
##### 3.1.4 Project
##### 3.1.5 Feature
##### 3.1.5 Milestone

#### 3.2 Working On Tasks

##### 3.2.1 Outside of Projects

##### 3.2.2 On Projects

###### 3.2.2.1 Milestones

Milestones are a way to group features of a project into work units. A milestone can contain many
features that must be completed for the project. The user enters
 
## 4. File Structure

##### 4.1 Task

micro-task stores task data in a text file defined by its task ID. For example, task 50 is in 50.txt. This file can be found in the folder belonging to the list that the task is on.

<b>Format (line by line):</b>

| Line | Description |
| ---- | ----------- |
| `name <text>` | a string defining the name of the task. |
| `state <enum>` | One of `Inactive`, `Active`, `Finished` |
| `recurring <bool>` | Either `true` or `false`, indicating <br>that the task is recurring and there for <br>cannot be finished. |
| `<blank>` | |
| `note <time> <text>` | Any number of notes can be defined here. |
| `<blank>` | |
| `add <time>` | The time at which the task was added. |
| | Start and stop are repeated together as many<br> times as the task is worked on. |
| `start <time>` | The time that the task was started.<br>  |
| `stop <time>` | The time that the task was stopped. |
| `finish <time>` | The time that the task was finished. |

##### 4.2 List

A list is a folder with its given name. For example, the list /default is stored in the default folder. Inside of this list folder there is any number of task files (task-<id>.txt) and a list.txt file that designates the folder as a list and defines list specific information.

<b>Format (line by line):</b>

| Line | Description |
| ---- | ----------- |
| `state <enum>` | `InProgress` or `Finished` |

##### 4.2 Group

A group is a folder with its given name, just like a list. For example, the group /projects/ is stored in the projects folder.
		Inside of this group folder there is any number of list folders and a group.txt file that designates the folder as a group and defines
		group specific information, similar to lists.

<b>Format (line by line):</b>

| Line | Description |
| ---- | ----------- |
| `state <enum>` | `InProgress` or `Finished` |

##### 4.2 Project

A project is a special form of group. Project folders are defined the same as group folders with an additional file, project.txt.
		The project.txt file defines a more detailed name for the project along with other information, such as any number of notes added
		by the user.
		
<b>Format (line by line):</b>

| Line | Description |
| ---- | ----------- |
| `name <text>` | Name of the project. This is more descriptive than the short name used for the file name. |
| `state <enum>` | `InProgress` or `Finished` |
| `note <time> <text>` | Any number of notes related to the project. |

##### 4.2 Feature

A feature is a special version of either a group or list folder. A feature starts out as a list but can be converted to a group
		if a sub-feature is added. A special feature.txt file is added to the group or list folder to give further details on the feature
		and to indicate that the group or list is a feature.

<b>Format (line by line):</b>

| Line | Description |
| ---- | ----------- |
| `state <enum>` | `InProgress` or `Finished` |
| `note <time> <text>` | Any number of notes related to the feature. |

##### 4.2 Milestone

Milestones are stored in a special milestones folder in the project group folder. For example /projects/<project>/milestones. This
		milestones folder is neither a list, nor a group. Instead the milestones folder is special and milestones is a reserved keyword that
		can not be used as a list or group name. In this folder are milestone files, these are named like so: milestone-<id>.txt. These files
		contain data relating to the milestone. For now this data is just the features that belong to the milestone, a description and an optional
		due date.

<b>Format (line by line):</b>

| Line | Description |
| ---- | ----------- |
| `state <enum>` | `InProgress` or `Finished` |
| `due <date>` | The due date for this milestone. <br>This is optional, if not set by the user then the line will read `due none`. |
| `note <time> <text>` | Any number of notes related to the milestone. |

## 5. Command Line Interface
micro-task is designed to run standalone from the command line or as an interactive program that continously takes input.
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

The start command allows the user to start a task, list or group.

#### 5.X Stop Command

The stop command allows the user to stop the active task, list or group.

#### 5.X Finish Command

The finish command allows the user to finish a task, list, group, project, feature or milestone.

#### 5.X Move Command

#### 5.X Search Command

#### 5.X Tasks Command

#### 5.X Times Command

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