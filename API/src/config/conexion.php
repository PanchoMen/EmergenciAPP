<?php  namespace App\config;

use PDO;

class db{
    private $hostname = 'localhost';
    private $database = 'EmergenciAPP';
    private $username = 'root';
    private $password = '';

    private $options = [
        PDO::ATTR_EMULATE_PREPARES   => false, // turn off emulation mode for "real" prepared statements
        PDO::ATTR_ERRMODE            => PDO::ERRMODE_EXCEPTION, //turn on errors in the form of exceptions
        PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC, //make the default fetch be an associative array
      ];

    public function connectDB(){
        $conexion = "mysql:host=$this->hostname; dbname=$this->database";
        $db = new PDO($conexion, $this->username, $this->password, $this->options);
        return $db;
    }
}
?>