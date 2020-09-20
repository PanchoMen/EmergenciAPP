<?php

namespace App\controllers;

use Psr\Http\Message\ResponseInterface as Response;
use Psr\Http\Message\ServerRequestInterface as Request;
use App\config\db;
use App\config\Firebase;
use PDO;

require_once __DIR__ . '/../config/conexion.php';
require_once __DIR__ . '/../config/firebase.php';

class notificationController
{
    public function setNotificationToken(Request $request, Response $response, $args)
    {
        $db = new db();
        $conexion = $db->connectDB();
        $arguments = array();
        $data = $request->getParsedBody();

        $arguments["id_user"] = isset($data['id']) ? $data['id'] : "NULL";

        $arguments["token"] = isset($data['token']) ? $data['token'] : "NULL";

        $sentencia =  $conexion->prepare("UPDATE Notifications_preferences SET notif_token= :token WHERE id_user = :id_user");
        $sentencia->bindParam(":id_user", $arguments["id_user"]);
        $sentencia->bindParam(":token", $arguments["token"]);
        $sentencia->execute();

        if ($sentencia->errorCode() == 0) {
            if ($sentencia->rowCount() > 0) {
                $response->getBody()->write(json_encode(array("result" => true, "message" => "Token actualizado correctamente"), JSON_UNESCAPED_UNICODE));
            } else {
                $response->getBody()->write(json_encode(array("result" => false, "message" => "Error al añadir el token"), JSON_UNESCAPED_UNICODE));
            }
        } else {
            $response->getBody()->write(json_encode(array("result" => false, "message" => $sentencia->errorInfo()), JSON_UNESCAPED_UNICODE));
        }

        $sentencia = NULL;
        $conexion = NULL;

        return $response;
    }

    public function setLocation(Request $request, Response $response, $args)
    {
        $db = new db();
        $conexion = $db->connectDB();
        $arguments = array();
        $data = $request->getParsedBody();

        $arguments["id_user"] = isset($data['id']) ? $data['id'] : "NULL";

        $arguments["latitude"] = isset($data['latitude']) ? $data['latitude'] : "NULL";

        $arguments["longitude"] = isset($data['longitude']) ? $data['longitude'] : "NULL";



        $sentencia =  $conexion->prepare('UPDATE Notifications_preferences SET latitude= :latitude, longitude= :longitude, lastLocationUpdate= CURRENT_TIMESTAMP  WHERE id_user = :id_user');
        $sentencia->bindParam(":id_user", $arguments["id_user"]);
        $sentencia->bindParam(":latitude", $arguments["latitude"]);
        $sentencia->bindParam(":longitude", $arguments["longitude"]);
        $sentencia->execute();

        if ($sentencia->errorCode() == 0) {
            $response->getBody()->write(json_encode(array("result" => true, "message" => "Ubicación actualizada correctamente"), JSON_UNESCAPED_UNICODE));
        } else {
            $response->getBody()->write(json_encode(array("result" => false, "message" => $sentencia->errorInfo()), JSON_UNESCAPED_UNICODE));
        }

        $sentencia = NULL;
        $conexion = NULL;

        return $response;
    }

    public function setRadius(Request $request, Response $response, $args)
    {
        $db = new db();
        $conexion = $db->connectDB();
        $arguments = array();
        $data = $request->getParsedBody();

        $arguments["id_user"] = isset($data['id']) ? $data['id'] : "NULL";

        $arguments["radius"] = isset($data['radius']) ? $data['radius'] : "NULL";

        $sentencia =  $conexion->prepare("UPDATE Notifications_preferences SET radius= :radius WHERE id_user = :id_user");
        $sentencia->bindParam(":id_user", $arguments["id_user"]);
        $sentencia->bindParam(":radius", $arguments["radius"]);
        $sentencia->execute();

        if ($sentencia->errorCode() == 0) {
            if ($sentencia->rowCount() > 0) {
                $response->getBody()->write(json_encode(array("result" => true, "message" => "Radio de notificación actualizado correctamente"), JSON_UNESCAPED_UNICODE));
            } else {
                $response->getBody()->write(json_encode(array("result" => false, "message" => "Error al añadir radio de notificación"), JSON_UNESCAPED_UNICODE));
            }
        } else {
            $response->getBody()->write(json_encode(array("result" => false, "message" => $sentencia->errorInfo()), JSON_UNESCAPED_UNICODE));
        }

        $sentencia = NULL;
        $conexion = NULL;

        return $response;
    }

    public function getRadius(Request $request, Response $response, $args)
    {
        $db = new db();
        $conexion = $db->connectDB();
    
        $id_user = $request->getAttribute("id");

        $sql1 = 'SELECT radius FROM Notifications_preferences
                WHERE id_user = :id_user';

        $sentencia =  $conexion->prepare($sql1);
        $sentencia->bindParam(":id_user", $id_user);
        $sentencia->execute();

        if ($sentencia->errorCode() == 0) {
            $result = $sentencia->fetch(PDO::FETCH_ASSOC);
            $response->getBody()->write(json_encode(array("result" => true, "message" => "Radio obtenido correctamente", "data" => $result["radius"]), JSON_UNESCAPED_UNICODE));
        }else {
            $response->getBody()->write(json_encode(array("result" => false, "message" => $sentencia->errorInfo()), JSON_UNESCAPED_UNICODE));
        }


        $sentencia = NULL;
        $conexion = NULL;

        return $response;
    }
}
