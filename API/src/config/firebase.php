<?php  namespace App\config;

define('FIREBASE_API_URL', 'https://fcm.googleapis.com/fcm/send');
define('FIREBASE_API_KEY', 'AAAAxZ41gwk:APA91bEdLbkaRYyPvVlRDwh56KYXtFhUx09j19IuyJhNa3LTujUeP69gXjsyTs9Rzp3oj537vLq7sBmLpvfmMOFlDd70D8Px2o47W0xNmBPZLvhcYmGHI9hHFceoA2_-PvNddMZOeGw8');

class Firebase {

    public function enviarMultiple($to = array(), $notification = array()){
        if(!empty($to)){array("result" => false, "message" => "Empty to array");}
        if(!empty($notification)){array("result" => false, "message" => "Empty notification");}

        foreach ($to as $token){
            $result = $this->enviarPushNotificacion($token, $notification);
            if(!$result["result"]){
                return $result;
            }
        }
        return $result;
    }

    public function enviarPushNotificacion($to, $notification = array()) {

        if(!isset($to)){return array("result" => false, "message" => "Empty to");}
        if(empty($notification)){return array("result" => false, "message" => "Empty notification");}

        $fields = array(
                'to' => $to,
                'notification' => array(
                                    'title' => $notification['title'],
                                    'body' => $notification['message'] . "\n" . $notification['timestamp']    
                ),
                'data' => $notification,
                'priority' => "high"
        );
        // Set POST variables
        $headers = array(
                    'Authorization: key=' . FIREBASE_API_KEY,
                    'Content-Type: application/json'
                    );
                    
        // Open connection
        $ch = curl_init();
        // Set the url, number of POST vars, POST data
        curl_setopt($ch, CURLOPT_URL, FIREBASE_API_URL);
        curl_setopt($ch, CURLOPT_POST, true);
        curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
        // Disabling SSL Certificate support temporarily
        curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
        curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($fields));       
        
        $result = curl_exec($ch);
        // Close connection
        curl_close($ch);
        if($result === FALSE) {
            return array("result" => false, "message" => 'Curl failed: ' . curl_error($ch));
        }else{
            return array("result" => true, "fields" => $fields);
        }
    }
}
?>