<?php

use FastRoute\RouteCollector;
use Slim\Routing\RouteCollectorProxy;

$app->group('/alert', function (RouteCollectorProxy $alert) {
    $alert->post('/sendAlert', 'App\controllers\alertController:sendAlert');

    $alert->get('/deleteAlert/{id}', 'App\controllers\alertController:deleteAlert');

    $alert->post('/acceptAlert', 'App\controllers\alertController:acceptAlert');

    $alert->get('/check/{id}', 'App\controllers\alertController:checkAlert');

    $alert->get('/historical/{id_user}', 'App\controllers\alertController:getHistorical');

    $alert->get('/getEstimatedTime/{id}', 'App\controllers\alertController:getEstimatedTime');
});
?>