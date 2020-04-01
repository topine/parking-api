#!/bin/bash

function park() {
  sleep $1;
  echo "Parking car"
  echo '{"regPlate": "plate-'"$2"'","type": "'"$3"'"}';
  curl -d '{"regPlate": "plate-'"$2"'","type": "'"$3"'"}' -H "Content-Type: application/json" -X POST http://parking-api:8080/v1/parkcar
  
}

function take() {
  sleep $1;
  echo "Retrieving car"
  echo '{"regPlate": "plate-'"$2"'","type": "'"$3"'"}';
  curl -d '{"regPlate": "plate-'"$2"'","type": "'"$3"'"}' -H "Content-Type: application/json" -X POST http://parking-api:8080/v1/takecar
  
}

function thermal() {

	for i in {1..10};
	  do 
	  	park $((i*2)) "t-$i" THERMAL
	done

	 for i in {1..10};
	  do 
	  	take $((i*2)) "t-$i" THERMAL
	done


}

function ev20() {

	for i in {1..10};
	  do 
	  	park $((i*2)) "ev20-$i" EV20KW
	done

	 for i in {1..10};
	  do 
	  	take $((i*2)) "ev20-$i" EV20KW
	done


}


function ev50() {

	for i in {1..15};
	  do 
	  	park $((i*2)) "ev50-$i" EV50KW
	done

	 for i in {1..15};
	  do 
	  	take $((i*2)) "ev50-$i" EV50KW
	done


}

while true
	do 	
	    thermal & 
	    ev20 &
	    ev50 &
		wait
done