# Performance Сoap Test Suite

## Getting started

Now you have an opportunity to independently evaluate the performance of **IoTBroker.Cloud**. 
Besides this test suite can be used to measure the performance of your own software. The following instruction will 
explain how to run the performance tests by yourself.

### Prerequisites

The following programs should be installed before starting to clone the project:

* **JDK (version 8+)**;
* **Maven**.

### Installation

First of all, you should clone [Performance COAP Test Suite](https://github.com/mobius-software-ltd/amqp-test-suite).

Then you have to build the project. For this purpose run in console "mvn clean install -Dgpg.skip=true" 

Now you have the controller (amqp-test-suite/controller/target/amqp-controller) and the test runner 
(amqp-test-suite/runner/target/amqp-scenario-runner)jar files on your computer.
To make the work more convenient, create _performance_test_ folder containing
`amqp-controller.jar` and `amqp-scenario-runner.jar`.
Also you should add [JSON files](https://github.com/mobius-software-ltd/amqp-test-suite/tree/master/runner/src/test/resources/json) and [config.properties](https://github.com/mobius-software-ltd/coap-test-suite/controller/src/main/resources/config.properties) to this very performance_test folder. 
Modify scenario file by setting "controller.1.ip", "controller.1.port", "broker.ip", "broker.port", "account.username", 
"account.password" with public IP addresses used on controller and broker.
In config.properties set "localHostname" property with local ip address of the machine running the controller.

### Test run

First you should open the terminal and `cd` to _performance_test_ folder. You should start the controller by running
the command which is given below (do not forget to indicate your path):
 

Now you can start the controller by running the following command :

```
java -Xmx1024m -Xms1024m -jar amqp-controller.jar
 
```
Here is a brief explanation:

**Xmx1024m** – maximum memory allocation;

**Xmx1024m** – initial memory allocation;

**controller.jar** – controller which is inside the _performance_test_ folder;


Now you should open the second terminal window and `cd` to _performance_test_ folder. 
Now you can run the test by running the following command:
```
java -jar test-runner.jar publishers_qos0.json
```
The command mentioned above is an example of running the test scenario which is described in `publishers_qos0.json` file.

Each [JSON file](https://github.com/mobius-software-ltd/amqp-test-suite/tree/master/runner/src/test/resources/json) contains different test scenarios. You can separately run each test scenario by indicating the name of a specific [JSON file](https://github.com/mobius-software-ltd/coap-test-suite/tree/master/runner/src/test/resources/json). When the test is over you will get the report for each test scenario:
```
+---------- Scenario-ID:  8d4e4778-4fd4-4810-bf57-39316593d09a ---------- Result: SUCCESS ----------+ 

| Start Time                      | 2019-01-04 16:44:52.940        | 1546613092940                  | 

| Finish Time                     | 2019-01-04 16:45:15.427        | 1546613115427                  | 

| Current Time                    | 2019-01-04 16:45:25.965        | 1546613125965                  | 

+---------------------------------+--------------------------------+--------------------------------+ 

| Total clients                 1 | Total commands               9 | Errors occured               0 | 

| Successfuly finished          1 | Successfuly finished         9 | Duplicates received          0 | 

| Failed                        0 | Failed                       0 | Duplicates sent              0 | 

+--------------- Outgoing counters ---------------+--------------- Incoming counters ---------------+ 

|      Counter Name      |      Counter Value     |      Counter Name      |      Counter Value     | 

|          PROTO         |            2           |          PROTO         |            2           | 

|          INIT          |            1           |       MECHANISMS       |            1           | 

|          OPEN          |            1           |          OPEN          |            1           | 

|          BEGIN         |            1           |          BEGIN         |            1           | 

|          PING          |            1           |         OUTCOME        |            1           | 

|         ATTACH         |            1           |         ATTACH         |            1           | 

|          FLOW          |            0           |          FLOW          |            0           | 

|        TRANSFER        |            0           |        TRANSFER        |          5000          | 

|       DISPOSITION      |          5000          |       DISPOSITION      |            0           | 

|         DETACH         |            1           |         DETACH         |            1           | 

|           END          |            1           |           END          |            1           | 

|          CLOSE         |            1           |          CLOSE         |            1           | 

+------------------------+------------------------+------------------------+------------------------+ 

  

+---------- Scenario-ID:  c0cec8c5-007f-4c1d-9436-af48f47745c6 ---------- Result: SUCCESS ----------+ 

| Start Time                      | 2019-01-04 16:44:57.974        | 1546613097974                  | 

| Finish Time                     | 2019-01-04 16:45:15.257        | 1546613115257                  | 

| Current Time                    | 2019-01-04 16:45:26.337        | 1546613126337                  | 

+---------------------------------+--------------------------------+--------------------------------+ 

| Total clients              1000 | Total commands           13000 | Errors occured               0 | 

| Successfuly finished       1000 | Successfuly finished     13000 | Duplicates received          0 | 

| Failed                        0 | Failed                       0 | Duplicates sent              0 | 

+--------------- Outgoing counters ---------------+--------------- Incoming counters ---------------+ 

|      Counter Name      |      Counter Value     |      Counter Name      |      Counter Value     | 

|          PROTO         |          2000          |          PROTO         |          2000          | 

|          INIT          |          1000          |       MECHANISMS       |          1000          | 

|          OPEN          |          1000          |          OPEN          |          1000          | 

|          BEGIN         |          1000          |          BEGIN         |          1000          | 

|          PING          |            0           |         OUTCOME        |          1000          | 

|         ATTACH         |          1000          |         ATTACH         |          1000          | 

|          FLOW          |            0           |          FLOW          |          1000          | 

|        TRANSFER        |          5000          |        TRANSFER        |            0           | 

|       DISPOSITION      |            0           |       DISPOSITION      |          5000          | 

|         DETACH         |            0           |         DETACH         |            0           | 

|           END          |          1000          |           END          |          1000          | 

|          CLOSE         |          1000          |          CLOSE         |          1000          | 

+------------------------+------------------------+------------------------+------------------------+
```
Each test can be run in its current form.
Besides you can change the existing test scenarios or add the new ones.

Performance MQTT-SN Test Suite is developed by [Mobius Software](http://mobius-software.com).

## [License](LICENSE.md)

