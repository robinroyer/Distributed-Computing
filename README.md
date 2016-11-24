# Distributed Computation Load Balancing

This project's goal is to implement a Java RMI distributed computation load balancing.
Many servers can connect to our load balancer and are available to receive calculous.
A server may or may not give the right answer and each server has it own capacity (**beware of overload !**)

In order to handle server overloading, the load balancer adjuts the number of calculatons sent to every server.

In order to handle malicious servers, the load balancer has 2 different modes `SAFE` & `UNSAFE`:
- `SAFE` => Server results are trustful and the repartitor only merges the results.
- `UNSAFE` => Server aren't trustful and the repartitor always verificates the server's results with cross-validation

## Compile this project

- Go to `v0` folder.
- compile sources : `ant`

## Launch servers
- launch rmiregistry on port **5010** from **./bin** : 
```Bash
# rmiregistry PORT &
rmiregistry 5010 &
```
- launch server : 
```Bash
# ./calculousServer IP PORT CONFIDENCE CAPACITY
./calculousServer 127.0.0.1 5010 0 5
```
## Launch load Balancer

- Server Ips and server ports configuration should be in **config/servers.config**:
```
127.0.0.1 5010
127.0.0.1 5011
190.0.0.1 5010
```
- Launch Load Balancer : 
```Bash
# ./repartitor [-S] : calculous are computed a 2nd time (cross-validation)
./repartitor -S
```

## Distribute computing

Once the load repartitor is launched, we have 2 options `compute FILE` & `exit`:
- `compute FILE` => parse the input file and distributes computing with the differents available servers, then print result
- `EXIT` => terminate load balancer



French report is available in **V0** folder : **Rapport-TP2-INF4410.pdf**


## Author

- [Jérémy Wimsingues](https://github.com/JWimsingues)
- [Robin Royer](https://github.com/robinroyer)
