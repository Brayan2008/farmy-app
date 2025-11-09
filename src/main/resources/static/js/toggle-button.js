abrirSidebar = () => {
  const controlador = document.querySelector('#sidebar-toggle');
  const sidebar = document.querySelector('#sidebar');
  controlador.style.display = 'none';
  sidebar.style.display = 'block';
  sidebar.style.width = '100vw';
}

cerrarSidebar = () => {

  console.log('cerrando');
  
  const controlador = document.querySelector('#sidebar-toggle');
  const sidebar = document.querySelector('#sidebar');
  controlador.style.display = 'flex';
  controlador.style.justifyContent = 'center';
  controlador.style.alignItems = 'center';
  sidebar.style.display = 'none';
}