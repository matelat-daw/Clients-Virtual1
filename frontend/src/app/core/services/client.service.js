/**
 * user.service.js - Servicio para gestionar usuarios
 */

class UserService {
    /**
     * Registra un nuevo usuario
     * @param {User} user - Objeto usuario
     * @param {File} profilePicture - Archivo de imagen de perfil (opcional)
     * @returns {Promise<Object>}
     */
    static async register(user, profilePicture = null) {
        try {
            const url = API_CONFIG.BASE_URL + API_CONFIG.ENDPOINTS.REGISTER;
            
            // Crear FormData para enviar datos + archivo
            const formData = new FormData();
            
            // Agregar todos los campos del usuario
            formData.append('nick', user.nick);
            formData.append('name', user.name);
            formData.append('surname1', user.surname1);
            if (user.surname2) formData.append('surname2', user.surname2);
            formData.append('email', user.email);
            formData.append('phone', user.phone);
            formData.append('password', user.password);
            formData.append('gender', user.gender);
            if (user.bday) formData.append('bday', user.bday);
            
            // Agregar archivo de imagen si existe
            if (profilePicture) {
                formData.append('profilePicture', profilePicture, profilePicture.name);
            }
// Enviar como FormData
            const response = await Utils.makeRequestWithFormData('POST', url, formData);
            return response;
        } catch (error) {
throw error;
        }
    }

    /**
     * Obtiene todos los usuarios con paginación
     * @param {number} page - Número de página (default 0)
     * @param {number} size - Tamaño de página (default 10)
     * @returns {Promise<Object>}
     */
    static async getUsers(page = 0, size = 10) {
        try {
            const url = `${API_CONFIG.BASE_URL}${API_CONFIG.ENDPOINTS.USERS}?page=${page}&size=${size}`;
            console.log(`📥 Cargando usuarios: page=${page}, size=${size}, URL=${url}`);
            
            // NO usar cache para usuarios ya que pueden cambiar frecuentemente
            // Siempre obtener datos frescos
            const response = await Utils.makeRequest('GET', url, null, false);
            
            console.log('📤 Respuesta completa del servidor:', {
                success: response.success,
                totalItems: response.pagination?.totalItems,
                users: response.users?.length
            });
            
            return response;
        } catch (error) {
            console.error('❌ Error cargando usuarios:', error);
            throw error;
        }
    }

    /**
     * Obtiene un usuario por ID
     * @param {number} id - ID del usuario
     * @returns {Promise<Object>}
     */
    static async getUserById(id) {
        try {
            const url = API_CONFIG.BASE_URL + API_CONFIG.ENDPOINTS.USER_BY_ID.replace(':id', id);
            
            console.log(`👤 Solicitando detalles del usuario ID: ${id}`);
            
            // NO usar cache para detalles - siempre obtener datos frescos
            // El caché causaba que se mostrara el usuario anterior
            const response = await Utils.makeRequest('GET', url, null, false);
            
            if (response.success && response.data) {
                console.log(`✅ Usuario encontrado: ${response.data.nick} (ID: ${response.data.id}, Email: ${response.data.email})`);
            }
            
            return response;
        } catch (error) {
            console.error(`❌ Error obtieniendo usuario ID ${id}:`, error);
            throw error;
        }
    }

    /**
     * Actualiza un usuario
     * @param {number} id - ID del usuario
     * @param {User} user - Objeto usuario con datos actualizados
     * @returns {Promise<Object>}
     */
    static async updateUser(id, user) {
        try {
            const url = API_CONFIG.BASE_URL + API_CONFIG.ENDPOINTS.UPDATE_USER.replace(':id', id);
            const response = await Utils.makeRequest('PUT', url, user.toJSON());
            
            // Limpiar cache tras actualizar
            Utils.clearCache();
            
            return response;
        } catch (error) {
throw error;
        }
    }

    /**
     * Elimina un usuario
     * @param {number} id - ID del usuario
     * @returns {Promise<Object>}
     */
    static async deleteUser(id) {
        try {
            console.log(`🗑️ Solicitando eliminación del usuario ID: ${id}`);
            const url = API_CONFIG.BASE_URL + API_CONFIG.ENDPOINTS.DELETE_USER.replace(':id', id);
            
            // Verificar que el usuario actual es ADMIN
            const userRole = AuthService.getRole();
            console.log('🔒 Rol del usuario actual:', userRole);
            
            if (userRole !== 'ADMIN') {
                throw new Error('❌ No tienes permisos para eliminar usuarios. Solo ADMIN puede hacerlo.');
            }
            
            const response = await Utils.makeRequest('DELETE', url);
            console.log('✅ Usuario eliminado exitosamente:', response);
            
            // Limpiar cache tras eliminar
            if (Utils.clearCache) {
                Utils.clearCache();
                console.log('🧹 Cache limpiado');
            }
            
            return response;
        } catch (error) {
            console.error('❌ Error eliminando usuario:', error);
            throw error;
        }
    }
}

// Exponer globalmente para la verificación de carga
window.UserService = UserService;
window.ClientService = UserService;

// Registrar que este script se ha cargado
if (typeof AppScripts !== 'undefined') AppScripts.register('client.service');


