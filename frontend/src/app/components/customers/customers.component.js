/**
 * CustomersComponent.js - Componente de gestión de customers
 */

class CustomersComponent {
    constructor() {
        this.selector = '#router-outlet';
        this.currentPage = 0;
        this.pageSize = 10;
        this.customers = [];
        this.totalItems = 0;
        this.totalPages = 0;
        this.customerToDelete = null;
        this.isSearching = false;
        this.searchFirstName = '';
        this.searchLastName = '';
        this.isLoading = false;
    }

    static getInstance() {
        if (!window.customersComponent) {
            window.customersComponent = new CustomersComponent();
        }
        return window.customersComponent;
    }

    async init() {
        try {
            console.log('🔄 Inicializando componente de customers...');

            // Verificar si el usuario está autenticado
            if (!AuthService.isAuthenticated()) {
                console.warn('Usuario no autenticado. Redirigiendo a login...');
                App.getInstance().navigateTo('/login');
                return;
            }

            // Obtener el rol del usuario
            const userRole = AuthService.getRole();
            const isAdmin = userRole === 'ADMIN';
            const isPremium = userRole === 'PREMIUM';

            if (!isAdmin && !isPremium) {
                this.renderAccessDenied(userRole);
                return;
            }

            this.userRole = userRole;
            this.isAdmin = isAdmin;
            this.isPremium = isPremium;

            // Cargar datos iniciales
            await this.loadCustomers();

            // Renderizar vista
            this.render();

            console.log('✅ Componente de customers inicializado correctamente');
        } catch (error) {
            console.error('❌ Error al cargar customers:', error);
            this.renderError(error.message);
        }
    }

    async loadCustomers() {
        this.isLoading = true;
        this.renderLoading();

        try {
            let response;
            if (this.isSearching) {
                if (this.searchFirstName) {
                    response = await CustomerService.searchByFirstName(this.searchFirstName, this.currentPage, this.pageSize);
                } else if (this.searchLastName) {
                    response = await CustomerService.searchByLastName(this.searchLastName, this.currentPage, this.pageSize);
                }
            } else {
                response = await CustomerService.getCustomers(this.currentPage, this.pageSize);
            }

            if (response && response.success) {
                const data = response.data || response;
                this.customers = data.customers || [];
                const pagination = data.pagination || {};
                
                this.totalItems = pagination.totalItems || 0;
                this.totalPages = pagination.totalPages || 0;
                this.currentPage = pagination.currentPage || 0;
            } else {
                throw new Error(response ? response.message : 'Error al obtener datos');
            }
        } catch (error) {
            console.error('Error loading customers:', error);
            throw error;
        } finally {
            this.isLoading = false;
        }
    }

    render() {
        const container = document.querySelector(this.selector);
        if (!container) return;

        const modeText = this.isAdmin ? 'Administración' : 'Visualización';
        
        container.innerHTML = `
            <div class="container-fluid py-5">
                <!-- Header y Stats -->
                <div class="row mb-4">
                    <div class="col-12">
                        <div class="d-flex justify-content-between align-items-center mb-3">
                            <div>
                                <h1 class="display-5 fw-bold mb-0">
                                    <i class="fas fa-users me-3 text-primary"></i>Customers MyIkea
                                </h1>
                                <p class="text-muted mt-2 mb-0">
                                    <span class="badge bg-info me-2">${this.userRole}</span>
                                    ${modeText} de la base de datos externa
                                </p>
                            </div>
                            <div class="text-end">
                                <div class="card bg-primary text-white shadow-sm">
                                    <div class="card-body py-2 px-4">
                                        <div class="small opacity-75">Total Customers</div>
                                        <div class="h4 mb-0 fw-bold">${this.totalItems}</div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Barra de búsqueda -->
                <div class="row mb-4">
                    <div class="col-12">
                        <div class="card shadow-sm border-0">
                            <div class="card-body bg-light rounded">
                                <div class="row g-3 align-items-end">
                                    <div class="col-md-4">
                                        <label class="form-label small fw-bold">Nombre</label>
                                        <div class="input-group">
                                            <span class="input-group-text bg-white border-end-0"><i class="fas fa-user text-muted"></i></span>
                                            <input type="text" class="form-control border-start-0" id="searchFirstName" placeholder="Ej: John" value="${this.searchFirstName}">
                                        </div>
                                    </div>
                                    <div class="col-md-4">
                                        <label class="form-label small fw-bold">Apellido</label>
                                        <div class="input-group">
                                            <span class="input-group-text bg-white border-end-0"><i class="fas fa-user-tag text-muted"></i></span>
                                            <input type="text" class="form-control border-start-0" id="searchLastName" placeholder="Ej: Doe" value="${this.searchLastName}">
                                        </div>
                                    </div>
                                    <div class="col-md-4">
                                        <div class="d-flex gap-2">
                                            <button class="btn btn-primary flex-grow-1" id="searchBtn">
                                                <i class="fas fa-search me-2"></i>Buscar
                                            </button>
                                            <button class="btn btn-outline-secondary" id="resetBtn" title="Limpiar filtros">
                                                <i class="fas fa-undo"></i>
                                            </button>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Tabla -->
                <div class="row">
                    <div class="col-12">
                        <div class="card shadow-sm border-0">
                            <div class="card-body p-0">
                                ${this.isLoading ? this.getLoadingHTML() : (this.customers.length > 0 ? this.renderTable() : this.renderEmpty())}
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Paginación -->
                ${!this.isLoading && this.totalPages > 1 ? this.renderPagination() : ''}
            </div>

            <!-- Modal: Confirmar eliminación -->
            <div class="modal fade" id="deleteConfirmModal" tabindex="-1">
                <div class="modal-dialog modal-dialog-centered">
                    <div class="modal-content border-0 shadow">
                        <div class="modal-header bg-danger text-white">
                            <h5 class="modal-title fw-bold">
                                <i class="fas fa-exclamation-triangle me-2"></i>Confirmar eliminación
                            </h5>
                            <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
                        </div>
                        <div class="modal-body py-4">
                            <p class="mb-1">¿Estás seguro de que deseas eliminar este customer?</p>
                            <h5 id="deleteCustomerInfo" class="fw-bold text-danger mb-3"></h5>
                            <div class="alert alert-warning mb-0 border-0">
                                <i class="fas fa-info-circle me-2"></i>Esta acción es irreversible.
                            </div>
                        </div>
                        <div class="modal-footer border-0">
                            <button type="button" class="btn btn-light" data-bs-dismiss="modal">Cancelar</button>
                            <button type="button" class="btn btn-danger px-4" id="confirmDeleteBtn">
                                <i class="fas fa-trash me-2"></i>Eliminar permanentemente
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        `;

        this.attachEventListeners();
    }

    renderTable() {
        const rows = this.customers.map(customer => `
            <tr>
                <td class="ps-4 py-3">
                    <div class="d-flex align-items-center">
                        <div class="avatar-circle me-3 bg-primary bg-opacity-10 text-primary">
                            ${(customer.firstName || '?').charAt(0)}${(customer.lastName || '?').charAt(0)}
                        </div>
                        <div>
                            <div class="fw-bold">${customer.firstName || 'N/A'} ${customer.lastName || ''}</div>
                            <div class="small text-muted">${customer.email || '-'}</div>
                        </div>
                    </div>
                </td>
                <td class="py-3">${customer.telefono || '-'}</td>
                <td class="py-3">
                    <span class="badge bg-light text-dark border small">
                        ${customer.fechaDeNacimiento ? new Date(customer.fechaDeNacimiento).toLocaleDateString() : 'N/A'}
                    </span>
                </td>
                <td class="py-3 text-end pe-4">
                    <div class="btn-group">
                        <button class="btn btn-sm btn-outline-primary" onclick="CustomersComponent.viewCustomer(${customer.customerId})">
                            <i class="fas fa-eye me-1"></i> Detalles
                        </button>
                        ${this.isAdmin ? `
                            <button class="btn btn-sm btn-outline-danger" onclick="CustomersComponent.confirmDelete(${customer.customerId}, '${customer.firstName} ${customer.lastName}')">
                                <i class="fas fa-trash"></i>
                            </button>
                        ` : ''}
                    </div>
                </td>
            </tr>
        `).join('');

        return `
            <div class="table-responsive">
                <table class="table table-hover align-middle mb-0">
                    <thead class="bg-light text-muted small text-uppercase fw-bold">
                        <tr>
                            <th class="ps-4 py-3">Customer</th>
                            <th class="py-3">Teléfono</th>
                            <th class="py-3">F. Nacimiento</th>
                            <th class="py-3 text-end pe-4">Acciones</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${rows}
                    </tbody>
                </table>
            </div>
        `;
    }

    renderEmpty() {
        const message = this.isSearching ? 
            'No se encontraron resultados para tu búsqueda.' : 
            'No hay customers registrados en el sistema.';
        
        return `
            <div class="text-center py-5 my-5">
                <div class="mb-4">
                    <i class="fas fa-search-minus display-1 text-muted opacity-25"></i>
                </div>
                <h4 class="fw-bold text-muted">${message}</h4>
                <p class="text-muted mb-4">Prueba con otros términos o limpia los filtros.</p>
                ${this.isSearching ? `
                    <button class="btn btn-primary" onclick="CustomersComponent.getInstance().handleReset()">
                        <i class="fas fa-sync-alt me-2"></i>Ver todos los customers
                    </button>
                ` : ''}
            </div>
        `;
    }

    renderPagination() {
        const pages = [];
        const maxVisible = 5;
        let start = Math.max(0, this.currentPage - Math.floor(maxVisible / 2));
        let end = Math.min(this.totalPages - 1, start + maxVisible - 1);

        if (end - start + 1 < maxVisible) {
            start = Math.max(0, end - maxVisible + 1);
        }

        // Botón Anterior
        pages.push(`
            <li class="page-item ${this.currentPage === 0 ? 'disabled' : ''}">
                <button class="page-link" onclick="CustomersComponent.goToPage(${this.currentPage - 1})">
                    <i class="fas fa-chevron-left"></i>
                </button>
            </li>
        `);

        for (let i = start; i <= end; i++) {
            pages.push(`
                <li class="page-item ${i === this.currentPage ? 'active' : ''}">
                    <button class="page-link" onclick="CustomersComponent.goToPage(${i})">${i + 1}</button>
                </li>
            `);
        }

        // Botón Siguiente
        pages.push(`
            <li class="page-item ${this.currentPage === this.totalPages - 1 ? 'disabled' : ''}">
                <button class="page-link" onclick="CustomersComponent.goToPage(${this.currentPage + 1})">
                    <i class="fas fa-chevron-right"></i>
                </button>
            </li>
        `);

        return `
            <div class="d-flex justify-content-center mt-4">
                <nav aria-label="Paginación de customers">
                    <ul class="pagination pagination-rounded shadow-sm">
                        ${pages.join('')}
                    </ul>
                </nav>
            </div>
        `;
    }

    renderLoading() {
        // Solo actualizar el contenido de la tabla para no perder el estado de los inputs
        const tableContainer = document.querySelector('.card-body.p-0');
        if (tableContainer) {
            tableContainer.innerHTML = this.getLoadingHTML();
        }
    }

    getLoadingHTML() {
        return `
            <div class="text-center py-5 my-5">
                <div class="spinner-border text-primary" role="status" style="width: 3rem; height: 3rem;">
                    <span class="visually-hidden">Cargando...</span>
                </div>
                <p class="mt-3 text-muted fw-bold">Cargando datos de clientes...</p>
            </div>
        `;
    }

    renderError(message) {
        const container = document.querySelector(this.selector);
        if (container) {
            container.innerHTML = `
                <div class="container py-5">
                    <div class="alert alert-danger shadow-sm border-0 d-flex align-items-center p-4">
                        <i class="fas fa-exclamation-circle display-6 me-4"></i>
                        <div>
                            <h4 class="alert-heading fw-bold">Error al cargar datos</h4>
                            <p class="mb-0">${message || 'No se pudo conectar con el servidor.'}</p>
                            <button class="btn btn-outline-danger mt-3" onclick="location.reload()">
                                <i class="fas fa-sync-alt me-2"></i>Reintentar
                            </button>
                        </div>
                    </div>
                </div>
            `;
        }
    }

    renderAccessDenied(userRole) {
        const container = document.querySelector(this.selector);
        if (container) {
            container.innerHTML = `
                <div class="container py-5 text-center">
                    <div class="row justify-content-center">
                        <div class="col-md-6">
                            <div class="card border-0 shadow-lg">
                                <div class="card-body py-5">
                                    <div class="mb-4 text-warning">
                                        <i class="fas fa-lock display-1"></i>
                                    </div>
                                    <h2 class="fw-bold mb-3">Acceso Restringido</h2>
                                    <p class="text-muted mb-4">
                                        Lo sentimos, tu rol <strong>${userRole}</strong> no tiene permisos para acceder a la base de datos de MyIkea.
                                    </p>
                                    <a href="/dashboard" class="btn btn-primary btn-lg px-5">
                                        <i class="fas fa-home me-2"></i>Volver al Inicio
                                    </a>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            `;
        }
    }

    attachEventListeners() {
        const searchBtn = document.getElementById('searchBtn');
        const resetBtn = document.getElementById('resetBtn');
        const searchFirstName = document.getElementById('searchFirstName');
        const searchLastName = document.getElementById('searchLastName');
        const confirmDeleteBtn = document.getElementById('confirmDeleteBtn');

        if (searchBtn) searchBtn.onclick = () => this.handleSearch();
        if (resetBtn) resetBtn.onclick = () => this.handleReset();
        
        const enterHandler = (e) => { if (e.key === 'Enter') this.handleSearch(); };
        if (searchFirstName) searchFirstName.onkeypress = enterHandler;
        if (searchLastName) searchLastName.onkeypress = enterHandler;

        if (confirmDeleteBtn) confirmDeleteBtn.onclick = () => this.executeDelete();
    }

    async handleSearch() {
        const firstName = document.getElementById('searchFirstName')?.value.trim() || '';
        const lastName = document.getElementById('searchLastName')?.value.trim() || '';

        if (!firstName && !lastName) {
            Utils.showMessage('Búsqueda', 'Ingresa al menos un criterio para buscar.', 'warning');
            return;
        }

        this.isSearching = true;
        this.searchFirstName = firstName;
        this.searchLastName = lastName;
        this.currentPage = 0;

        try {
            await this.loadCustomers();
            this.render();
        } catch (error) {
            Utils.showMessage('Error', 'No se pudo completar la búsqueda.', 'error');
        }
    }

    async handleReset() {
        this.isSearching = false;
        this.searchFirstName = '';
        this.searchLastName = '';
        this.currentPage = 0;
        
        try {
            await this.loadCustomers();
            this.render();
        } catch (error) {
            Utils.showMessage('Error', 'No se pudieron restablecer los datos.', 'error');
        }
    }

    // Métodos estáticos llamados desde el HTML
    static async goToPage(page) {
        const instance = CustomersComponent.getInstance();
        instance.currentPage = page;
        try {
            await instance.loadCustomers();
            instance.render();
        } catch (error) {
            Utils.showMessage('Error', 'Error al cambiar de página.', 'error');
        }
    }

    static viewCustomer(id) {
        App.getInstance().navigateTo(`/customers/${id}`);
    }

    static confirmDelete(id, name) {
        const instance = CustomersComponent.getInstance();
        instance.customerToDelete = id;
        
        const info = document.getElementById('deleteCustomerInfo');
        if (info) info.textContent = name;
        
        const modal = new bootstrap.Modal(document.getElementById('deleteConfirmModal'));
        modal.show();
    }

    async executeDelete() {
        if (!this.customerToDelete) return;

        try {
            const response = await CustomerService.deleteCustomer(this.customerToDelete);
            if (response.success) {
                Utils.showMessage('Éxito', 'Customer eliminado correctamente.', 'success');
                
                // Cerrar modal
                const modalElement = document.getElementById('deleteConfirmModal');
                const modal = bootstrap.Modal.getInstance(modalElement);
                modal.hide();

                // Recargar lista
                await this.loadCustomers();
                this.render();
            } else {
                throw new Error(response.message);
            }
        } catch (error) {
            Utils.showMessage('Error', error.message || 'No se pudo eliminar el customer.', 'error');
        } finally {
            this.customerToDelete = null;
        }
    }
}

// Exponer globalmente e inicializar
window.CustomersComponent = CustomersComponent;

// Estilos dinámicos para el avatar y la paginación rounded
if (!document.getElementById('customer-styles')) {
    const style = document.createElement('style');
    style.id = 'customer-styles';
    style.innerHTML = `
        .avatar-circle {
            width: 40px;
            height: 40px;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            font-weight: bold;
            font-size: 14px;
        }
        .pagination-rounded .page-link {
            border-radius: 50% !important;
            margin: 0 3px;
            width: 38px;
            height: 38px;
            display: flex;
            align-items: center;
            justify-content: center;
            border: none;
            color: #6c757d;
        }
        .pagination-rounded .page-item.active .page-link {
            background-color: #0d6efd;
            color: white;
        }
        .pagination-rounded .page-link:hover {
            background-color: #e9ecef;
        }
    `;
    document.head.appendChild(style);
}
