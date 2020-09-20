<?php

use FastRoute\RouteCollector;
use Slim\Routing\RouteCollectorProxy;

$app->group('/users', function (RouteCollectorProxy $users) {
    $users->get('/login/{email}/{password}', 'App\controllers\usersController:login');

    $users->post('/register', 'App\controllers\usersController:register');

    $users->post('/update', 'App\controllers\usersController:updateData');

    $users->get('/id/{email}', 'App\controllers\usersController:getID');

    $users->get('/data/{id}', 'App\controllers\usersController:getData');

    $users->get('/userData/{id}', 'App\controllers\usersController:getMedicalData');

    $users->get('/medicalData/{id}', 'App\controllers\usersController:getMedicalData');    
    
    $users->group('/notificationPreferences', function(RouteCollectorProxy $notificationPref){

        $notificationPref->post('/token', 'App\controllers\notificationController:setNotificationToken');

        $notificationPref->post('/location', 'App\controllers\notificationController:setLocation');

        $notificationPref->post('/radius', 'App\controllers\notificationController:setRadius');

        $notificationPref->get('/radius/{id}', 'App\controllers\notificationController:getRadius');
    });
});
?>

