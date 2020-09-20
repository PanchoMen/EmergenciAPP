<?php

namespace App\controllers;

use Psr\Http\Message\ResponseInterface as Response;
use Psr\Http\Message\ServerRequestInterface as Request;
use App\config\db;
use App\config\Firebase;
use PDO;

require_once __DIR__ . '/../config/conexion.php';
require_once __DIR__ . '/../config/firebase.php';

define('OPENROUTESERVICE_API_KEY', '5b3ce3597851110001cf6248de58e36e9d28470688727a33b79f400b');
define('OPENROUTESERVICE_URL', 'https://api.openrouteservice.org/v2/matrix/foot-walking');


class alertController
{
    public function sendAlert(Request $request, Response $response, $args)
    {
        $db = new db();
        $conexion = $db->connectDB();
        $arguments = array();
        $data = $request->getParsedBody();
        $firebase = new Firebase();
        $to = array();
        $notification = array();

        $arguments['id_user'] = isset($data['id']) ? $data['id'] : "NULL";

        $arguments['latitude'] = isset($data['latitude']) ? $data['latitude'] : 0.0;

        $arguments['longitude'] = isset($data['longitude']) ? $data['longitude'] : 0.0;

        $notification['title'] = isset($data['title']) ? $data['title'] : "🚑 ¡AYUDA!";

        $notification['message'] = isset($data['message']) ? $data['message'] : "Necesito asistencia médica";

        $notification['timestamp'] = (isset($data['title']) && isset($data['message'])) ? "" : date('d-m-Y G:i:s e');

        $notification['action'] = isset($data['action']) ? $data['action'] : "Alert";

        $notification['latitude'] = $arguments['latitude'];

        $notification['longitude'] = $arguments['longitude'];

        $notification["id_user"] = $arguments['id_user'];

        $sql1 = 'INSERT INTO Alerts(id_user, user_latitude, user_longitude)
                VALUES (:id_user, :user_latitude, :user_longitude)';

        $sql2 = 'SELECT id_user, notif_token, ( 6371 * acos(cos(radians(:alert_lat)) * cos(radians(latitude)) * cos(radians(longitude) - radians(:alert_lon)) + sin(radians(:alert_lat2)) * sin(radians(latitude)))) AS distance, radius/1000 AS radio
                FROM (SELECT id_user, notif_token, latitude, longitude, lastLocationUpdate, radius
                        FROM (SELECT * FROM Users WHERE Users.user_type <> 1) As U
                        LEFT JOIN Notifications_preferences 
                        ON U.id = Notifications_preferences.id_user) AS P
                WHERE P.id_user <> :id_user
                HAVING distance < radio 
                ORDER BY distance';

        $sentencia1 = $conexion->prepare($sql1);
        $sentencia1->bindParam(":id_user", $arguments["id_user"]);
        $sentencia1->bindParam(":user_latitude", $arguments["latitude"]);
        $sentencia1->bindParam(":user_longitude", $arguments["longitude"]);

        $sentencia1->execute();

        $sentencia2 =  $conexion->prepare($sql2);  //"SELECT * FROM Notifications_preferences WHERE id_user <> :id_user");
        $sentencia2->bindParam(":id_user", $arguments["id_user"]);
        $sentencia2->bindParam(":alert_lat", $arguments["latitude"]);
        $sentencia2->bindParam(":alert_lat2", $arguments["latitude"]);
        $sentencia2->bindParam(":alert_lon", $arguments["longitude"]);

        if ($sentencia1->errorCode() == 0) {
            $id_alert = $conexion->lastInsertId();
            $to = $sentencia2->fetchAll(PDO::FETCH_COLUMN, 1);
            $sentencia2->execute();

            if ($sentencia2->rowCount() == 0) {
                $response->getBody()->write(json_encode(array("result" => false, "message" => "No hay asistencia disponible actualmente"), JSON_UNESCAPED_UNICODE));
            } else {
                $to = $sentencia2->fetchAll(PDO::FETCH_COLUMN, 1);
                $notification["id_alert"] = $id_alert;
                $result = $firebase->enviarMultiple($to, $notification);
                $result["data"] = array("id" => $id_alert);
                $response->getBody()->write(json_encode($result, JSON_UNESCAPED_UNICODE));
            }
        } else {
            $response->getBody()->write(json_encode(array("result" => false, "message" => "Error al crear la alerta"), JSON_UNESCAPED_UNICODE));
        }
        $sentencia1 = NULL;
        $sentencia2 = NULL;
        $conexion = NULL;

        return $response;
    }

    public function acceptAlert(Request $request, Response $response, $args)
    {
        $db = new db();
        $conexion = $db->connectDB();
        $arguments = array();
        $data = $request->getParsedBody();
        $firebase = new Firebase();
        $to = array();
        $notification = array();

        $arguments['id_alert'] = isset($data['id_alert']) ? $data['id_alert'] : "NULL";

        $arguments['id_sanitary'] = isset($data['id_sanitary']) ? $data['id_sanitary'] : "NULL";

        $arguments['latitude'] = isset($data['latitude']) ? $data['latitude'] : "NULL";

        $arguments['longitude'] = isset($data['longitude']) ? $data['longitude'] : "NULL";

        $notification['title'] = isset($data['title']) ? $data['title'] : "Internal Notification";

        $notification['message'] = isset($data['message']) ? $data['message'] : "Alert accepted";

        $notification['timestamp'] = (isset($data['title']) && isset($data['message'])) ? "" : date('d-m-Y G:i:s e');

        $notification['action'] = isset($data['action']) ? $data['action'] : "Response";

        $sql =  'SELECT a.id,
                        a.id_user,
                        u.user_token,
                        a.id_sanitary,
                        s.sanitary_token,
                        a.user_latitude,
                        a.user_longitude,
                        a.sanitary_latitude,
                        a.sanitary_longitude,
                        a.accepted
                FROM   (SELECT *
                        FROM   Alerts
                        WHERE  Alerts.id = :id_alert) AS a
                        LEFT JOIN (SELECT notifications_preferences.id_user,
                                        notifications_preferences.notif_token AS user_token
                                FROM   notifications_preferences) AS u
                            ON a.id_user = u.id_user
                        LEFT JOIN (SELECT notifications_preferences.id_user,
                                        notifications_preferences.notif_token AS sanitary_token
                                FROM   notifications_preferences) AS s
                            ON a.id_user = s.id_user';
        $sentencia1 = $conexion->prepare($sql);
        $sentencia1->bindParam(":id_alert", $arguments["id_alert"]);

        $sentencia2 = $conexion->prepare("UPDATE Alerts SET id_sanitary= :id_sanitary, sanitary_latitude= :sanitary_latitude, sanitary_longitude= :sanitary_longitude, accepted= CURRENT_TIMESTAMP  WHERE id = :id_alert");
        $sentencia2->bindParam(":id_alert", $arguments["id_alert"]);
        $sentencia2->bindParam(":id_sanitary", $arguments["id_sanitary"]);
        $sentencia2->bindParam(":sanitary_latitude", $arguments["latitude"]);
        $sentencia2->bindParam(":sanitary_longitude", $arguments["longitude"]);

        $sentencia1->execute();

        if ($sentencia1->errorCode() == 0) {
            if ($sentencia1->rowCount() == 0){
                $response->getBody()->write(json_encode(array("result" => false, "message" => "La solicitud ha sido cancelada por el usuario, gracias por su ayuda", "data" => NULL), JSON_UNESCAPED_UNICODE));
            }else{
                $resul = $sentencia1->fetch(PDO::FETCH_ASSOC);
                if ($resul) {
                    if (empty($resul["accepted"])) {
                        $to = $resul["user_token"];
                        $sentencia2->execute();
                        if ($sentencia2->rowCount() == 1) {
                            $sentencia1->execute();
                            $resul = $sentencia1->fetch(PDO::FETCH_ASSOC);
                            $notification = array_merge($notification, $resul);
                            $result = $firebase->enviarPushNotificacion($to, $notification);
                            $result["data"] = $resul;
                            $response->getBody()->write(json_encode($result, JSON_UNESCAPED_UNICODE));
                        } else {
                            $response->getBody()->write(json_encode(array("result" => false, "message" => "Error al aceptar la solicitud", "data" => NULL), JSON_UNESCAPED_UNICODE));
                        }
                    } else {
                        $response->getBody()->write(json_encode(array("result" => false, "message" => "La solicitud ya ha sido aceptada por otro usuario, gracias por su ayuda", "data" => $resul), JSON_UNESCAPED_UNICODE));
                    }
                } else {
                    $response->getBody()->write(json_encode(array("result" => false, "message" => "Error al aceptar la solicitud", "data" => NULL), JSON_UNESCAPED_UNICODE));
                }
            }
        } else {
            $response->getBody()->write(json_encode(array("result" => false, "message" => "Error al aceptar la solicitud", "data" => NULL), JSON_UNESCAPED_UNICODE));
        }

        $sentencia1 = NULL;
        $sentencia2 = NULL;
        $conexion = NULL;

        return $response;
    }

    public function getHistorical(Request $request, Response $response, $args)
    {
        $db = new db();
        $conexion = $db->connectDB();
        $id_user = $request->getAttribute("id_user");

        $sql1 = 'SELECT * FROM Alerts
                WHERE id_user = :id_user';

        $sql2 = 'SELECT * FROM Alerts
                WHERE id_sanitary = :id_sanitary';

        $sentencia1 = $conexion->prepare($sql1);
        $sentencia1->bindParam(":id_user", $id_user);
        
        $sentencia2 = $conexion->prepare($sql2);
        $sentencia2->bindParam(":id_sanitary", $id_user);

        $sentencia1->execute();
        $sentencia2->execute();

        $solicitudes = array();
        $asistencias = array();
        if ($sentencia1->errorCode() == 0 && $sentencia2->errorCode() == 0){
            $solicitudes = $sentencia1->fetchAll(PDO::FETCH_ASSOC);
            $asistencias = $sentencia2->fetchAll(PDO::FETCH_ASSOC);

            $response->getBody()->write(json_encode(array("result" => true, "message" => "Historial obtenido correctamente", "data" => array($solicitudes, $asistencias)), JSON_UNESCAPED_UNICODE));
        } else {
            $response->getBody()->write(json_encode(array("result" => false, "message" => "Error obteniendo el historial", "data" => NULL), JSON_UNESCAPED_UNICODE));
        }

        $sentencia1 = NULL;
        $conexion = NULL;

        return $response;

        
    }

    public function getEstimatedTime(Request $request, Response $response, $args)
    {
        $db = new db();
        $conexion = $db->connectDB();
        $id_alert = $request->getAttribute("id");

        $sql = 'SELECT  a.id_user,
                        user_latitude,
                        user_longitude,
                        a.id_sanitary,
                        sanitary_latitude,
                        sanitary_longitude
                FROM   (SELECT id_user,
                                id_sanitary
                        FROM   Alerts
                        WHERE  id = :id_alert) AS a
                        LEFT JOIN (SELECT notifications_preferences.id_user,
                                        notifications_preferences.latitude  AS user_latitude,
                                        notifications_preferences.longitude AS user_longitude
                                FROM   notifications_preferences) AS u
                            ON a.id_user = u.id_user
                        LEFT JOIN (SELECT notifications_preferences.id_user   AS id_sanitary,
                                        notifications_preferences.latitude  AS
                                        sanitary_latitude,
                                        notifications_preferences.longitude AS
                                        sanitary_longitude
                                FROM   notifications_preferences) AS s
                            ON a.id_sanitary = s.id_sanitary';

        $sentencia1 = $conexion->prepare($sql);
        $sentencia1->bindParam(":id_alert", $id_alert);

        $sentencia1->execute();

        $resul = $sentencia1->fetch(PDO::FETCH_ASSOC);
        if ($resul) {
            $headers = array(
                'Authorization:' . OPENROUTESERVICE_API_KEY,
                'Content-Type: application/json'
                );

            $fields = json_encode(array(
                'locations' => array(
                    array(
                        $resul["sanitary_longitude"],
                        $resul["sanitary_latitude"]
                    ),
                    array(
                        $resul["user_longitude"],
                        $resul["user_latitude"]
                    )
                ),
                'destinations' => array(
                    1
                )
            ));

            $ch = curl_init();
            // Set the opts
            curl_setopt($ch, CURLOPT_URL, OPENROUTESERVICE_URL);
            curl_setopt($ch, CURLOPT_POST, true);
            curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
            curl_setopt($ch, CURLOPT_POSTFIELDS, $fields);
            curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
            $result = json_decode(curl_exec($ch), JSON_UNESCAPED_UNICODE);
            // Close connection
            curl_close($ch);
            if(!empty($result)){
                $min = floatval(reset($result['durations'])[0]) / 60.0;
                $response->getBody()->write(json_encode(array("result" => true, "message" => "Tiempo calculados correctamente", "data" => $min), JSON_UNESCAPED_UNICODE));
            } else {
                $response->getBody()->write(json_encode(array("result" => false, "message" => "Error en la API", "data" => NULL), JSON_UNESCAPED_UNICODE));
            }
        } else {
            $response->getBody()->write(json_encode(array("result" => false, "message" => "Error en la consulta", "data" => NULL), JSON_UNESCAPED_UNICODE));
        }

        $sentencia1 = NULL;
        $conexion = NULL;

        return $response;
    }

    public function deleteAlert(Request $request, Response $response, $args)
    {
        $db = new db();
        $conexion = $db->connectDB();
        $id_alert = $request->getAttribute("id");

        $sql = 'DELETE FROM Alerts
                WHERE id = :id_alert';
        $sentencia1 = $conexion->prepare($sql);
        $sentencia1->bindParam(":id_alert", $id_alert);

        $sentencia1->execute();

        if ($sentencia1->errorCode() == 0) {
            $response->getBody()->write(json_encode(array("result" => true, "message" => "Alerta cancelada correctamente"), JSON_UNESCAPED_UNICODE));
        }else{
            $response->getBody()->write(json_encode(array("result" => false, "message" => "Error al cancelar la solicitud"), JSON_UNESCAPED_UNICODE));
        }

        $sentencia1 = NULL;
        $conexion = NULL;

        return $response;
    }
}
