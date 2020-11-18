# GVE_DevNet_Jabber_Custom_Tab_Shared_Lines_Status
Jabber custom tab to show line status for shared lines using JTapi. 

## Contacts
* Gerardo Chaves (gchaves@cisco.com)
* Rami Alfadel (ralfadel@cisco.com)

## Solution Components
* Cisco Unified Communications Manager
* Cisco Jabber

## Installation/Configuration

### Requirements

- [OpenJDK](https://openjdk.java.net/) 11

- [Apache Maven](https://maven.apache.org/) 3.6.3

- [Visual Studio Code](https://code.visualstudio.com/)

- A working Cisco Unified Communications Manager environment:

    - An CUCM application-user or end-user username/password, with the following roles:

        - `Standard CTI Allow Control of Phones supporting Connected Xfer and conf`

        - `Standard CTI Allow Control of Phones supporting Rollover Mode`

        - `Standard CTI Enabled`

        - `Standard CTI Allow Control of all Devices`

    - At least one [CTI suported phone devices](https://developer.cisco.com/site/jtapi/documents/cti-tapi-jtapi-supported-device-matrix/) (i.e. Jabber soft phones),  configured with at least one shared directory number.

        >Note, ensure at least one directory number has `Allow Control of Device from CTI` enabled

### Getting Started

 1. Make sure you have OpenJDK 11 installed, `java` is available in the path, and `$JAVA_HOME` points to the right directory:

    ```bash
    $ java -version
    openjdk 11.0.8 2020-07-14
    OpenJDK Runtime Environment (build 11.0.8+10-post-Ubuntu-0ubuntu120.04)
    OpenJDK 64-Bit Server VM (build 11.0.8+10-post-Ubuntu-0ubuntu120.04, mixed mode, sharing)
    ```

    ```bash
    $ echo $JAVA_HOME
    /usr/lib/jvm/java-1.11.0-openjdk-amd64
    ```

2. Open a terminal and use `git` to clone this repository

    ```bash
    git clone https://wwwin-github.cisco.com/gve/GVE_DevNet_Jabber_Custom_Tab_Shared_Lines_Status.git
    ```

3. Open the Java project in [Visual Studio Code](https://code.visualstudio.com/):

    ```bash
    cd GVE_DevNet_Jabber_Custom_Tab_Shared_Lines_Status
    code .
    ```

4. Edit rename `.env.example` to `.env`, and edit to specify environment variable config for the samples you wish to run.

5. Copy the `sharedlines.html` file from this project into  the target\classes\templates folder. It should have been created already. 

6. Copy the `pom.xml` file into the jtapi folder (or edit the existing one to add the javalin entries)

7. In `monitorLines.java` on line 102, specify the shared line extensions to monitor by DN. For example:
```String[] lineDNs = { "6016", "6017", "6020" };```

8. Create a custom tab on the Jabber client where you would like to monitor the shared lines

![CreateTab1](/IMAGES/CreateCustomTab1.png)

Enter the URL for the web server implemented for this sample. If you are running the JTapi code on the same PC as where  
Jabber is running it would be:  http://localhost:7000/getSharedLines  , but for any other Jabber clients on other PCs you would need to replace localhost with the hostname or IP address of the web server. 

### NOTE: This URL is also currently hard coded in `sharedlines.html` so you need to change the hostname from localhost to the IP or hostname of the web server in that file as well before trying to access the custom tab from Jabber on other PCs   

![CreateTab2](/IMAGES/CreateCustomTab2.png)

9. Finally, to launch the sample in VS Code, select the **Run** panel, choose the desired `Launch...` option from the drop-down in the upper left, and click the green 'Start Debugging' arrow (or hit **F5**)

![Launch](/IMAGES/launch.png)



## Usage

Once the project is running, it will start monitoring the shared lines specified in the lineDNs[] array and showing their status in the Jabber custom tab. 

![AllIdle](/IMAGES/AllIdle.png)

If a call is being offered on one of the monitored shared lines, the color of the button will change and the callerID information will be shown:

![Alerting](/IMAGES/Alerting.png)

If the call is answered, it will show both lines as connected:

![Connected](/IMAGES/Connected.png)

 
## Notes

1. In this project, the 11.5 and 12.5 versions of the JTAPI Java library have been deployed to the project's local Maven repo (in `lib/`), with 12.5 being the configured version. 

    If you want to use 11.5 (or you deploy another version, as below), modify `pom.xml` to specify the desired JTAPI version dependency.  Modify `<version>`:

    ```xml
    <dependency>
        <groupId>com.cisco.jtapi</groupId>
        <artifactId>jtapi</artifactId>
        <version>12.5</version>
    </dependency>
    ```

1.  If  you want to use another JTAPI version in the project:

    * Download and install/extract the JTAPI plugin from CUCM (**Applications** / **Plugins**)

    * From this repository's root, use Maven to deploy the new version of `jtapi.jar` to the local repo.  You will need to identify the full path to the new `jtapi.jar` installed above:

        ```bash
        mvn deploy:deploy-file -DgroupId=com.cisco.jtapi -DartifactId=jtapi -Dversion={version} -Durl=file:./lib -DrepositoryId=local-maven-repo -DupdateReleaseInfo=true -Dfile={/path/to/jtapi.jar}
        ```

        >Note: be sure to update {version} and {/path/to/jtapi.jar} to your actual values

1. JTAPI configuration - e.g. trace log number/size/location and various timeouts - can be configured in `jtapi_config/jtapi.ini` (defined as a resource in `pom.xml`)

1. As of v12.5, the Cisco `jtapi.jar` does not implement the [Java Platform Module System](https://www.oracle.com/corporate/features/understanding-java-9-modules.html) (JPMS).  See this [issue](https://github.com/CiscoDevNet/jtapi-samples/issues/1) for more info.



### LICENSE

Provided under Cisco Sample Code License, for details see [LICENSE](LICENSE.md)

### CODE_OF_CONDUCT

Our code of conduct is available [here](CODE_OF_CONDUCT.md)

### CONTRIBUTING

See our contributing guidelines [here](CONTRIBUTING.md)

#### DISCLAIMER:
<b>Please note:</b> This script is meant for demo purposes only. All tools/ scripts in this repo are released for use "AS IS" without any warranties of any kind, including, but not limited to their installation, use, or performance. Any use of these scripts and tools is at your own risk. There is no guarantee that they have been through thorough testing in a comparable environment and we are not responsible for any damage or data loss incurred with their use.
You are responsible for reviewing and testing any scripts you run thoroughly before use in any non-testing environment.