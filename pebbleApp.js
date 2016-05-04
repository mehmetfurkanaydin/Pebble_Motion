var UI = require('ui');
var ajax = require('ajax');



// Create a Card with title and subtitle
var card = new UI.Card({
    title:'Motion',
    subtitle:'From Android..'
});

// Display the Card
card.show();

// Construct URL

var URL = 'https://xxxxxxx';

// Make the request
ajax(
    {
        url: URL,
        type: 'json',

    },
    function(data) {
        // Success!
        console.log("Successfully Added Values!");

        function getMax(arr, prop) {
            var max;
            for (var i=0 ; i<arr.length ; i++) {
                if (!max || parseFloat(arr[i][prop]) > parseFloat(max[prop]))
                    max = arr[i];
            }
            return max;
        }

        var maxPpg = getMax(data, "currentValue");
        console.log(maxPpg.date + " - Active Time " + maxPpg.currentValue);



        var result=maxPpg.date + " - Active Time " + maxPpg.currentValue;
        // Extract data
        card.body(result);
        console.log(maxPpg.currentValue);
        console.log(result);




    },
    function(error) {
        // Failure!
        console.log('Failed fetching  data: ' + error);
    }
);
