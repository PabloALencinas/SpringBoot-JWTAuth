# Notas para la Autentication mediante JWT

## 1 - Application Properties

Configuracion de la base de datos que vamos a utilizar, en este caso, MySQL:

    spring.jpa.hibernate.ddl-auto=update
    spring.datasource.url=jdbc:mysql://localhost:3306/securitydb
    spring.datasource.username=ROOTUSERNAME
    spring.datasource.password=ROOTPASSWORD
    spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect


## 2 - Creacion del paquete "CONTROLADOR" 

Package Controller: para la autenticacion, con los endpoints para hacer el login o el registro de usuarios
Agregamos los endpoints necesarios que utilizaremos para la autenticacion.

## 3 - Cracion del paquete "DEMO"

Este paquete tendra los endpoints que van a estar "protegidos".
Y con esto tenemos los endpoints necesarios para realizar las pruebas

PD: Por defecto, spring security protege todos los endpoints de la aplicacion
Por lo que vamos a crear las configuraciones relacionadas a los FILTROS

Para iniciaremos un nuevo paquete "Config"

## 3 - Creacion del paquete "Config" con las clases de configuracion pertinentes

Creamos entonces la clase de configuracion del security de spring: "SecurityConfig"
Y esta clase es la que contendra la "CADENA DE FILTROS" (SecurityFilterChain)

Dentro de la clase SecurityConfig debemos colocar las anotaciones: @Configuration, @EnableWebSecurity y
@RequiredArgsConstructor

Ahora implementaremos el metodo SecurityFilterChain que contendra toda la cadena de filtros que se van a ir ejecutando
Implementado el authrizedHttpRequest con una funcion lambda para encadenar los filtros a ejecutar

Primero para los endpoints que son publicos y despues para los endpoints protegidos, deshailitando tambien el csrf
csrf: Cross Site Request Forgery. Medida de seguridad que se utiliza para agregar a las solicitudes POST una autenticacion
basada en un TOKEN csrf valido. Esto se deshabilita ya que nuestra autenticacion se basa mediante JWT no en un token generado
por el csrf

Una vez generado esto ya podemos probar los endpoints mediante Postman

## Testando los endpoints de la API con postman. Ok!

## JSON Web Token - TOKEN DE AUTENTICACION no de AUTORIZACION.

String que consta de 3 partes separados por un punto:

    . Encabezado: Tipo de token y el tipo de algoritmo utilizado para firmarlo
    . Payload: Contiene los datos de sesion como user, roles, permisos.. etc.
    . Signature: Firma para verificar la integridad del token y que no haya sido alterado. 
        Contiene la CLAVE SECRETA (SECRET_KEY)

La autorizacion debe realizarse de manera independiente en el servicio de la api

Proceso de Autenticacion/Registro

1 - Iniciamos con una peticion HTTP por parte del cliente, luego
2 - Se ejecutara un FILTRO (JwtAuthenticationFilter) que se encargara de toda la validacion del TOKEN. 
    Check si el JWT es 'null', cuando verfica, el filtro concluye y pasa a la siguiente etapa
3 - Se da lugar luego del FILTRO al CONTROLADOR DE AUTENTICACION (AuthenticationController): Este controlador
    va a INVOCAR al servicio de AUTENTICATION (AuthenticationService) que, si se desea registrar un nuevo usuario, 
    se va a encargar de GUARDAR el registro de usuario en la BASE DE DATOS mediante el UserRepository (LA ENTIDAD USER
    DEBE IMPLEMENTAR LA INTERFAZ UserDetails). Y para el login, el SERVICIO se encargara de buscar en la BASE DE DATOS
    al usuario correspondiente. Luego de esto
4 - Finalmente GENERARA EL TOKEN mediante la clase SERVICIO (JwtService) y lo devolvera al AUTHENTICATION CONTROLLER quien, 
    a su vez devolvera la RESPUESTA al CLIENTE con el TOKEN en el CUERPO DEL MENSAJE

Como se implementa esto dentro de app? Seguimos en las siguientes secciones

## Creacion de un nuevo Paquete: Jwt y la clase dentro: JwtAuthenticationFilter

Creamos entonces el paquete con la clase correspondiente al FILTRO DE AUTENTICACION (JwtAuthenticationFilter)
Dicha clase va a 'extender' de la clase OncePerRequestFilter (Filtro que se ejecutara solo una vez x peticion)
, sobreescribiendo tambien el metodo 'doFilterInternal' que funciona, justamente, como FILTRO del JSON Web Token


doFilterInternal: Realizara todos los filtros relacionados al TOKEN. En el filtro debemos OBTENER EL TOKEN desde el 
request (HttpServltRequest request y response, tambien el filterChain). 
Una vez implementado el filtro con el getTokenFromRequest, nos dirigimos el CONTROLADOR de AUTENTICATION (AuthController)

En AuthController, primero debemos especificar como va a 'esperar' el controlador los request y los response, para esto
crearemos 3 CLASES para esto (Dentro del mismo paquete):

    . LoginRequest: Clase encargada del login del usuario
    . RegisterRequest: Clase para el Registro de usuarios
    . AuthResponse: Clase que devolvera el token para la autenticacion

Una vez implementados, configuramos los endpoints relacionados con el LOGIN y con el RESPONSE
ResponseEntity: Representa la respuesta a las peticiones HTTP, incluyendo codigo de estado, encabezado y codigo de respuesta


El response entity devolvera un objecto ResponseEntity el CUAL VA A SER UNA RESPUESTA DESDE EL SERVICIO DE AUTENTICACION 
ya sea para el login o para el registro de nuevos users: Para esto implementamos la clase servicion para la autenticacion.

Clase AuthenticationService para manejar las respuestas para el login y para el register de usuario.
Para implementar esto necesitaremos INTERACTUAR con el MODELO DEL USER en la BASE DE DATOS, creando e implementando la logica
del REPOSITORIO que se encargara de la conexion a la BD (UserRepository y el User MODEL):

    . Para esto creamos un nuevo paquete: user con las clases:
        - Role: para determinar el ROL que tendra el usuario
        - User: clase del MODEL para la base de datos
        - UserRepository: heredara el JpaRepository para tener acceso a las operaciones sobre usuarios para la BD

Una vez finalizada la implementacion de estos metodos, volvemos al Servicio del Authenticador (AuthService) y seguimos 
con la implementacion de los metodos login y register

    . Register: Instanciamos un nuevo objecto User y asignamos los valores del atributo a dicho objeto para luego
    guardarlo en la base de datos. Para la completitud del registro debemos enviar el usuario Y EL TOKEN GENERADO
    MEDIANTE EL SERVICIO QUE MANEJA LAS OPERACIONES PARA EL JWT.

    . JwtService: Clase servicio que nos permitira realizar toda la operacion necesaria para generar nuestro JWT token
    Dentro de esta clase implementamos el metodo getToken para generar el token a guardar para el register del user.
    PRIMERO PARA ESTO DEBEMOS AGREGAR LAS DEPENDENCIAS DE JWT EN EL POM.XML !!!!! IMPORTANTE !!!!!!!

Ahora, dentro de la clase JwtService implementaremos los metodos necesarios para las operaciones:

    .getToken: metodo para generar y obtener el token JWT para el user. Asi como tambien el:
    .getKey: para convertir nuestra SECRET_KEY(aleatoria) en BASE 64 y enviarla al token para firmar y obtener dicho token.

    Entonces el path quedaria asi: 
        JwtService (genera token) -> AuthService (Logica del Auth) -> Controlador (Registra Usuario)

    PERO TODAVIA HAY QUE PROVEERLE A SPRING BOOT CUAL ES EL "AUTHENTICATION MANAGER" QUE TIENE QUE UTILIZAR EN CUANTO A 
    PROVEEDOR. EN ESTE CASO, DEBE SER UN PROVEEDOR DE ACCESO A DATOS Y EN CASO DEL PASSWORD ENCODER DEBEMOS ESPECIFICAR
    CUAL ES EL ALGORITMO QUE TIENE QUE UTILIZAR PARA, FINALMENTE, CODIFICAR EL PASSWORD Y LLEVARLO ENCRYPTADO A LA BD.

Para esto, creamos una nueva clase en nuestro paquete de configuracion: "ApplicationConfig" con los siguientes metodos:

    . authenticationManager: Metodo que nos permite acceder a la instancia de auth manager y devolverla y el
    . authenticationProvider: Metodo que nos permite acceder al provider y retornarlo. El cual sera el DaoAuthenticationProvider
        => Para este metodo necesitamos crear otros dos para hacer la correcta devolucion del Provider:
            . userDetailService: Nos devolvera el username del usuario y si no existe nos tira un exception de not found
            . passwordEncoder: Codifica la password mediante BCryptPasswrodEncoder()

Despues de esta implementacion no debemos olvidarnos del SecurityConfig donde tenemos configurado toda la secuencia de FILTROS

    . En esta security config teniamos la configuracion que trabajaba CON SPRING SECURITY, AQUI:
        -> .formLogin(Customizer.withDefaults()) ..
    . Debemos reemplazar esta configuracion que CORRESPONDE A SPRING SECURITY, y AGREGAR LA CONFIGURACION DE JWT:
        -> .sessionManagement(sessionManager ->
						sessionManager
								.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authenticationProvider(authProvider)
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

Una vez realizado esto, podemos testear nuevamente nuestra aplicacion de spring con POSTMAN
Y FUNCIONA PERFECTO EL REGISTER DE USER CON JWT!

Ahora, luego del REGISTRO, nos toca implementar el LOGIN para el usuario.

Nos dirigimos al Servicio del Authenticador "AuthService" y vamos a implementar lo siguiente para el metodo login:

    . Creamos una instancia del Authentication Manager: authenticationManager porque necesitamos que el usuario se autentique
    . Llamamos al metodo authenticate del authenticationManager
    . Si el usuario se acredito exitosamente, generamos el JWT token correspondiente

ENTONCES, COMO funciona el flujo de autentication mediante JWT

CLIENTE recibio el token Y LO ALMACENA LOCALMENTE 
    -> Dicho token almacenado localmente SE UTILIZARA PARA ACCEDER A LOS ENDPOINTS PROTEGIDOS.. y como se realiza esto?
    pues INYECTANDO EL TOKEN en la peticion HTTP (especialmente en el encabezado(HEADER))

PATH:

CLIENTE: Peticion HTTP con el TOKEN ASOCIADO en el encabezado
-> LUEGO, pasa por el JWTAUTHENTICATIONFILTER: que chequea este JWT, si este no EXISTE o no lo encuentra = 403 Forbidden
-> SI EXISTE DICHO TOKEN, como paso siguiente, VA A EXTRAR el username DEL TOKEN JWT de la peticion mediante la clase de 
validacion JwtService y verifica si lo puede obtener del SECURITYCONTEXTHOLDER y SINO puede obtenerlo del context holder
-> LO VA A BUSCAR a la BASE DE DATOS utilizando el UserDetailService (loadUserByUsername()), una vez OBTENIDO EL USER
-> CHEQUEA QUE EL TOKEN ESTE CORRECTO (JwtService -> JwtAuthenticationFilter), si falla = 403 FORBIDDEN, SI ESTA CORRECTO
-> ACTUALIZA EL SECURITYCONTEXTHOLDER y dara acceso valido al CONTROLADOR que devolvera la respuesta en formato JSON u otro
formato al cliente

Luego de esto, debemos implementar el acceso para el endpoint protegido (DemoController)
vamos de nuevo a nuestra clase JwtAuthenticationFilter, y agregamos:

NOTACION: PARA FIX DEL private final ... AGREGAR EL @RequiredArgsConstructor en la CLASE! IMPORTANTE!!!!!!

    . LOS SERVICIOS:
        -> JwtService y el
        -> UserDetailService 
    . y CON ESTO DEBEMOS implementar lo siguiente dentro del doFilterInternal:

        // Acceder al username si funciona
        username = jwtService.getUsernameFromToken(token);

        Si no lo encontramos en el security context holder, debemos buscarlo en la BD:
        Una vez hecho esto, VALIDAMOS el token mediante el metodo del jwtService.isTokenValid
        Con lo siguiente:

                // Si es valido, actualizamos el security context holder
				UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
						userDetails,
						null,
						userDetails.getAuthorities()
				);
				// Seteamos el details
				authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

				// Obtenemos el contexto del security context holder y seteamos la autenticacion
				SecurityContextHolder.getContext().setAuthentication(authToken);

        Luego:
        // Que continue con el filtro
		filterChain.doFilter(request, response);

Una vez hecho esto debemos implementar en el servicio del jwt token, los metodos:

    . isValidToken
    . getUsernameFromToken

    Para el isTokenValid necesitamos implementar estos metodos:
        . getAllClaims para obtener todos los claims
        . getClaim: metodo generico para obtener un claim especifico
        . getExpiration: para Obtener la expiracion del token
        . isTokenExpired: booleano que nos devolvera si el token ha expirado o no

    Luego de esto, implementamos la logica del isValidToken con la logica pertinente.

Luego hecho esto, podemos una vez mas testear nuestra aplicacion con POSTMAN.

Este testeo va a ser el endpoint PROTEGIDO ("api/v1/demo") y DEBO PASAR EL TOKEN en la PESTANA AUTHORIZATION de POSTMAN
en el campo "BEARER TOKEN" y.. FUNCIONA! podemos acceder al endpoint protegido por JwtToken!

# EXTRAS

## Metodo para modificar informacion del usuario autenticado con JWT

Implementacion clase "UpdateRequest" para los atributos que se van a modificar dentro de la BD para los usuarios
Implementacion de la clase UserService y del metodo update dentro para modificar sus datos
Y la agregacion del controlador que va a manear la peticion Http para la modificacion. Endpoint Protegido luego del Login




        

