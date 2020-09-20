<?php

namespace App\controllers;

use Psr\Http\Message\ResponseInterface as Response;
use Psr\Http\Message\ServerRequestInterface as Request;
use App\config\db as db;

require_once __DIR__ . '/../config/conexion.php';
global $conexion;

class usersController
{
    public function login(Request $request, Response $response, $args)
    {
        $db = new db();
        $conexion = $db->connectDB();
        $email = $request->getAttribute("email");
        $password = $request->getAttribute("password");

        $sql = 'SELECT * FROM 
                    (SELECT Users.id, Users.email, Users.password, Users.user_type as user_typeID, User_type.descripcion as user_type
                    FROM Users NATURAL JOIN User_type WHERE email = :email) AS U 
                JOIN Users_data ON U.id = Users_data.id_user 
                JOIN Users_medicalData ON U.id = Users_medicalData.id_user';

        $sentencia =  $conexion->prepare($sql);
        $sentencia->bindParam(":email", $email);
        $sentencia->execute();

        if ($sentencia->rowCount() == 0) {
            $response->getBody()->write(json_encode(array("result" => false, "message" => "Usuario no encontrado", "data" =>  NULL), JSON_UNESCAPED_UNICODE));
        } else {
            $resul = $sentencia->fetch();
            $login = password_verify($password, $resul['password']);
            unset($resul['password']);
            unset($resul['id_user']);
            $resul['fecha_nacimiento'] = date("d/m/Y", strtotime($resul['fecha_nacimiento']));
            if ($login) {
                $response->getBody()->write(json_encode(array("result" => $login, "message" => "Login correcto", "data" => $resul), JSON_UNESCAPED_UNICODE));
            } else {
                $response->getBody()->write(json_encode(array("result" => $login, "message" => "Contraseña incorrecta", "data" => NULL), JSON_UNESCAPED_UNICODE));
            }
        }
        $sentencia = NULL;
        $conexion = NULL;

        return $response;
    }

    public function register(Request $request, Response $response, $args)
    {
        $db = new db();
        $conexion = $db->connectDB();

        $arguments = array();
        $data = $request->getParsedBody();

        $arguments["dni"] = isset($data['dni']) ? $data['dni'] : "NULL";

        $arguments["email"] = isset($data['email']) ? $data['email'] : "NULL";

        $arguments["password"] = isset($data['password']) ? password_hash($data['password'], PASSWORD_DEFAULT) : "NULL";

        $arguments["user_type"] = isset($data['user_type']) ? $data['user_type'] : "NULL";

        $arguments["nombre"] = isset($data['nombre']) ? $data['nombre'] : "NULL";

        $arguments["apellidos"] = isset($data['apellidos']) ? $data['apellidos'] : "NULL";

        $arguments["telefono"] = isset($data['telefono']) ? $data['telefono'] : "NULL";

        $arguments["fecha_nacimiento"] = isset($data['fecha_nacimiento']) ? date("Y-m-d", strtotime(preg_replace('/\\//i', '-', $data['fecha_nacimiento']))) : "NULL";

        $arguments["sexo"] = isset($data['sexo']) ? $data['sexo'] : "NULL";

        $arguments["grupo_sanguineo"] = isset($data['grupo_sanguineo']) ? $data['grupo_sanguineo'] : "NULL";

        $arguments["alergias"] = isset($data['alergias']) ? $data['alergias'] : "NULL";

        $arguments["otros"] = isset($data['otros']) ? $data['otros'] : "NULL";

        $id_user = "NULL";

        $sql = "INSERT INTO Users(email, password, user_type) VALUES (:email, :password, :user_type)";
        $sql2 = "INSERT INTO Users_data(id_user, nombre, apellidos, dni, telefono, fecha_nacimiento, sexo) VALUES (:id_user, :nombre, :apellidos, :dni, :telefono, :fecha_nacimiento, :sexo)";
        $sql3 = "INSERT INTO Users_medicalData(id_user, grupo_sanguineo, alergias, otros) VALUES (:id_user, :grupo_sanguineo, :alergias, :otros)";
        $sql4 = "INSERT INTO Notifications_preferences(id_user, lastLocationUpdate) VALUES(:id_user, NULL)";

        $conexion->beginTransaction();
        $sentencia1 = $conexion->prepare($sql);
        $sentencia1->bindParam(':email', $arguments["email"]);
        $sentencia1->bindParam(':password', $arguments["password"]);
        $sentencia1->bindParam(':user_type', $arguments["user_type"]);

        $sentencia2 = $conexion->prepare($sql2);
        $sentencia2->bindParam(':id_user', $id_user);
        $sentencia2->bindParam(':nombre', $arguments["nombre"]);
        $sentencia2->bindParam(':apellidos', $arguments["apellidos"]);
        $sentencia2->bindParam(':dni', $arguments["dni"]);
        $sentencia2->bindParam(':telefono', $arguments["telefono"]);
        $sentencia2->bindParam(':fecha_nacimiento', $arguments["fecha_nacimiento"]);
        $sentencia2->bindParam(':sexo', $arguments["sexo"]);

        $sentencia3 = $conexion->prepare($sql3);
        $sentencia3->bindParam(':id_user', $id_user);
        $sentencia3->bindParam(':grupo_sanguineo', $arguments["grupo_sanguineo"]);
        $sentencia3->bindParam(':alergias', $arguments["alergias"]);
        $sentencia3->bindParam(':otros', $arguments["otros"]);
        
        $sentencia4 = $conexion->prepare($sql4);
        $sentencia4->bindParam(':id_user', $id_user);

        $sentencia1->execute();
        $id_user = $conexion->lastInsertId();
        $sentencia2->execute();
        $sentencia3->execute();
        $sentencia4->execute();
        if ($sentencia1->errorCode() == 0 && $sentencia2->errorCode() == 0 && $sentencia3->errorCode() == 0 && $sentencia4->errorCode() == 0) {
            $conexion->commit();
            $response->getBody()->write(json_encode(array("result" => true, "message" => "Usuario " . $id_user . " creado", "data" => array("id" => intval($id_user))), JSON_UNESCAPED_UNICODE));
            $response = $response->withStatus(201);
        } else {
            $response->getBody()->write(json_encode(array("result" => false, "message" => "Error al crear el usuario, revise los campos e inténtelo de nuevo", "data" => NULL), JSON_UNESCAPED_UNICODE));
        }

        $sentencia1 = NULL;
        $sentencia2 = NULL;
        $sentencia3 = NULL;
        $sentencia4 = NULL;
        $conexion = NULL;

        return $response;
    }

    public function updateData(Request $request, Response $response, $args){
        $db = new db();
        $conexion = $db->connectDB();

        $arguments = array();
        $data = $request->getParsedBody();

        $arguments["id"] = isset($data['id']) ? $data['id'] : "NULL";

        //$arguments["email"] = isset($data['email']) ? $data['email'] : "NULL";

        //$arguments["password"] = isset($data['password']) ? password_hash($data['password'], PASSWORD_DEFAULT) : "NULL";

        //$arguments["user_type"] = isset($data['user_type']) ? $data['user_type'] : "NULL";

        $arguments["nombre"] = isset($data['nombre']) ? $data['nombre'] : "NULL";

        $arguments["apellidos"] = isset($data['apellidos']) ? $data['apellidos'] : "NULL";

        $arguments["dni"] = isset($data['dni']) ? $data['dni'] : "NULL";

        $arguments["telefono"] = isset($data['telefono']) ? $data['telefono'] : "NULL";

        $arguments["fecha_nacimiento"] = isset($data['fecha_nacimiento']) ? date("Y-m-d", strtotime(preg_replace('/\\//i', '-', $data['fecha_nacimiento']))) : "NULL";

        $arguments["sexo"] = isset($data['sexo']) ? $data['sexo'] : "NULL";

        $arguments["grupo_sanguineo"] = isset($data['grupo_sanguineo']) ? $data['grupo_sanguineo'] : "NULL";

        $arguments["alergias"] = isset($data['alergias']) ? $data['alergias'] : "NULL";

        $arguments["otros"] = isset($data['otros']) ? $data['otros'] : "NULL";

        $sql1 = "UPDATE Users_data SET nombre = :nombre, apellidos = :apellidos, dni = :dni, telefono = :telefono, fecha_nacimiento = :fecha_nacimiento, sexo = :sexo WHERE id_user = :id_user";
        $sql2 = "UPDATE Users_medicalData SET grupo_sanguineo = :grupo_sanguineo, alergias = :alergias, otros = :otros WHERE id_user = :id_user";
        $sql3 = 'SELECT *
                FROM Users_data
                JOIN Users_medicalData ON Users_data.id_user = Users_medicalData.id_user
                WHERE Users_data.id_user = :id_user';

        $conexion->beginTransaction();
        $sentencia1 = $conexion->prepare($sql1);
        $sentencia1->bindParam(':id_user', $arguments["id"]);
        $sentencia1->bindParam(':nombre', $arguments["nombre"]);
        $sentencia1->bindParam(':apellidos', $arguments["apellidos"]);
        $sentencia1->bindParam(':dni', $arguments["dni"]);
        $sentencia1->bindParam(':telefono', $arguments["telefono"]);
        $sentencia1->bindParam(':fecha_nacimiento', $arguments["fecha_nacimiento"]);
        $sentencia1->bindParam(':sexo', $arguments["sexo"]);

        $sentencia2 = $conexion->prepare($sql2);
        $sentencia2->bindParam(':id_user', $arguments["id"]);
        $sentencia2->bindParam(':grupo_sanguineo', $arguments["grupo_sanguineo"]);
        $sentencia2->bindParam(':alergias', $arguments["alergias"]);
        $sentencia2->bindParam(':otros', $arguments["otros"]);

        $sentencia3 = $conexion->prepare($sql3);
        $sentencia3->bindParam(':id_user', $arguments["id"]);

        $sentencia1->execute();
        $sentencia2->execute();

        if ($sentencia1->errorCode() == 0 && $sentencia2->errorCode() == 0){
            $conexion->commit();
            $sentencia3->execute();
            $resul = $sentencia3->fetch();
            $resul['id'] = $resul['id_user'];
            $resul['fecha_nacimiento'] = date("d/m/Y", strtotime($resul['fecha_nacimiento']));
            unset($resul['id_user']);
            $response->getBody()->write(json_encode(array("result" => true, "message" => "Usuario " . $resul["id"] . " actualizado correctamente", "data" => $resul), JSON_UNESCAPED_UNICODE));
        }else{
            $response->getBody()->write(json_encode(array("result" => false, "message" => "Error al actualizar el usuario, revise los campos e inténtelo de nuevo", "data" => NULL), JSON_UNESCAPED_UNICODE));
        }     

        $sentencia1 = NULL;
        $sentencia2 = NULL;
        $sentencia3 = NULL;
        $conexion = NULL;

        return $response;
    }

    public function getData(Request $request, Response $response, $args)
    {
        $db = new db();
        $conexion = $db->connectDB();
        $id_user = $request->getAttribute("id");

        //TODO make query
        $sentencia =  $conexion->prepare("SELECT * FROM (SELECT Users.id, Users.email FROM Users) AS U JOIN Users_data ON U.id = Users_data.id_user JOIN Users_medicalData ON U.id = Users_medicalData.id_user WHERE id = :id_user");
        $sentencia->bindParam(":id_user", $id_user);
        $sentencia->execute();

        $numRows = $sentencia->rowCount();
        if ($numRows == 0) {
            $response->getBody()->write(json_encode(array("result" => false, "message" => "Usuario no encontrado", "data" => NULL), JSON_UNESCAPED_UNICODE));
        } else {
            $resul = $sentencia->fetch();
            unset($resul['id_user']);
            $response->getBody()->write(json_encode(array("result" => true, "message" => "", "data" => $resul), JSON_UNESCAPED_UNICODE));
        }

        $sentencia = NULL;
        $conexion = NULL;

        return $response;
    }

    public function getID(Request $request, Response $response, $args)
    {
        $db = new db();
        $conexion = $db->connectDB();
        $email = $request->getAttribute("email");

        $sentencia =  $conexion->prepare("SELECT id FROM Users WHERE email = :email");
        $sentencia->bindParam(":email", $email);
        $sentencia->execute();

        $numRows = $sentencia->rowCount();
        if ($numRows == 0) {
            $response->getBody()->write(json_encode(array("result" => false, "message" => "Usuario no encontrado", "data" => NULL), JSON_UNESCAPED_UNICODE));
        } else {
            $resul = $sentencia->fetch();
            $response->getBody()->write(json_encode(array("result" => true, "message" => "", "data" => $resul), JSON_UNESCAPED_UNICODE));
        }

        $sentencia = NULL;
        $conexion = NULL;

        return $response;
    }
}
