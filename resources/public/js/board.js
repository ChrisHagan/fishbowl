var camera, scene, renderer;
var cameraDistance = 2500;
var material;
var side = 100;
var controls;


init();
animate();

function initTrackball(){
    controls = new THREE.OrbitControls(camera,renderer.domElement); 
}

function init(){
    var width = 400;
    var height = 400;
    camera = new THREE.PerspectiveCamera(75, width / height, 1, 10000);
    camera.position.y = 700;
    camera.position.z = cameraDistance;

    scene = new THREE.Scene();

    material = new THREE.MeshBasicMaterial({color:0xff0000, wireframe: true});
    for(var col = -10; col < 10; col++){
        for(var row = -10; row < 10; row++){
            for(var z = 0; z < Math.random() * 10; z++){
                addCube(col,row,z, scene);
            }
        }
    }

    renderer = new THREE.CanvasRenderer();
    renderer.setSize(width, height);

    document.getElementById("board").appendChild(renderer.domElement);
    initTrackball();
}

function addCube(x,z,y,scene){
    var geometry = new THREE.CubeGeometry(side,side,side);
    var mesh = new THREE.Mesh(geometry,material);
    mesh.position.x = x * side;
    mesh.position.z = z * side;
    mesh.position.y = y * side;
    scene.add(mesh);
}

function animate(){
    requestAnimationFrame(animate);
    controls.update();
    renderer.render(scene,camera);
}
