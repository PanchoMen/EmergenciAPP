<?php
use Selective\BasePath\BasePathMiddleware;
use Slim\Factory\AppFactory;

require_once __DIR__ . '/../vendor/autoload.php';

$app = AppFactory::create();

$app->addRoutingMiddleware();
$app->add(new BasePathMiddleware($app));
$app->addErrorMiddleware(true, true, true);

require_once __DIR__ . '/../src/rutas/users.php';
require_once __DIR__ . '/../src/rutas/alert.php';

$app->run();
?>